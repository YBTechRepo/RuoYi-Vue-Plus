package org.dromara.insurance.service;

import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.TenantConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.insurance.config.ApplicationFormExportProperties;
import org.dromara.insurance.domain.InsuranceApplyRecord;
import org.dromara.insurance.domain.InsuranceApplicationExportTask;
import org.dromara.insurance.domain.InsurancePolicy;
import org.dromara.insurance.domain.bo.InsuranceApplicationExportRequest;
import org.dromara.insurance.domain.bo.InsuranceApplyRecordBo;
import org.dromara.insurance.domain.vo.InsuranceApplicationExportTaskVo;
import org.dromara.insurance.domain.vo.InsuranceApplyRecordVo;
import org.dromara.insurance.mapper.InsuranceApplicationExportTaskMapper;
import org.dromara.insurance.mapper.InsuranceApplyRecordMapper;
import org.dromara.insurance.mapper.InsurancePolicyMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class InsuranceApplicationExportService {
    static final String PENDING = "PENDING";
    static final String RUNNING = "RUNNING";
    static final String SUCCESS = "SUCCESS";
    static final String PARTIAL = "PARTIAL";
    static final String FAILED = "FAILED";
    static final String EXPIRED = "EXPIRED";
    private static final String PLATFORM_TENANT = TenantConstants.DEFAULT_TENANT_ID;
    private static final long MANIFEST_RESERVE_BYTES = 1024L * 1024;
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final List<String> INFORMATION_HEADERS = List.of(
        "序号", "订单号", "保单号", "产品名称", "起保日期",
        "投保人姓名", "投保人证件类型", "投保人证件号", "投保人证件生效期", "投保人证件到期日", "投保人手机号", "投保人地址",
        "与投保人关系", "被保人姓名", "被保人证件类型", "被保人证件号", "被保人证件生效期", "被保人证件到期日", "被保人手机号", "被保人地址"
    );

    private final InsuranceApplicationExportTaskMapper taskMapper;
    private final InsuranceApplyRecordMapper orderMapper;
    private final InsurancePolicyMapper policyMapper;
    private final IInsuranceProxyOrderService proxyOrderService;
    private final InsuranceApplicationFormService applicationFormService;
    private final ApplicationFormStorage storage;
    private final ApplicationFormExportProperties properties;
    private final ObjectMapper json;
    private final ScheduledExecutorService scheduledExecutorService;
    private final TaskExecutor executor;

    public InsuranceApplicationExportService(InsuranceApplicationExportTaskMapper taskMapper,
                                             InsuranceApplyRecordMapper orderMapper,
                                             InsurancePolicyMapper policyMapper,
                                             IInsuranceProxyOrderService proxyOrderService,
                                             InsuranceApplicationFormService applicationFormService,
                                             ApplicationFormStorage storage,
                                             ApplicationFormExportProperties properties,
                                             ObjectMapper json,
                                             ScheduledExecutorService scheduledExecutorService,
                                             @Qualifier("applicationFormExportExecutor") TaskExecutor executor) {
        this.taskMapper = taskMapper;
        this.orderMapper = orderMapper;
        this.policyMapper = policyMapper;
        this.proxyOrderService = proxyOrderService;
        this.applicationFormService = applicationFormService;
        this.storage = storage;
        this.properties = properties;
        this.json = json;
        this.scheduledExecutorService = scheduledExecutorService;
        this.executor = executor;
    }

    @PostConstruct
    public void scheduleRecoveryAndCleanup() {
        scheduledExecutorService.scheduleWithFixedDelay(this::scanSafely, 10, 60, TimeUnit.SECONDS);
    }

    public InsuranceApplicationExportTaskVo create(InsuranceApplicationExportRequest request) {
        Long userId = LoginHelper.getUserId();
        InsuranceApplicationExportTask existing = taskByRequest(userId, request.getRequestId());
        if (existing != null) return toVo(existing);

        long active = ignore(() -> taskMapper.selectCount(Wrappers.<InsuranceApplicationExportTask>lambdaQuery()
            .eq(InsuranceApplicationExportTask::getTenantId, PLATFORM_TENANT)
            .eq(InsuranceApplicationExportTask::getCreateBy, userId)
            .in(InsuranceApplicationExportTask::getStatus, PENDING, RUNNING)));
        if (active > 0) throw new ServiceException("已有投保单导出任务正在处理，请等待完成后再创建");

        List<Long> targetIds = resolveTargetIds(request);
        if (targetIds.isEmpty()) throw new ServiceException("没有符合条件的代投保订单");
        if (targetIds.size() > properties.getMaxOrders()) {
            throw new ServiceException("单次最多导出" + properties.getMaxOrders() + "个订单，请缩小范围");
        }

        InsuranceApplicationExportTask task = new InsuranceApplicationExportTask();
        task.setTenantId(PLATFORM_TENANT);
        task.setActiveUserId(userId);
        task.setRequestId(request.getRequestId());
        task.setScope(request.getScope());
        task.setCriteriaJson(encode("FILTER".equals(request.getScope()) ? request.getQuery() : Map.of("orderIds", targetIds)));
        task.setTargetIdsJson(encode(targetIds));
        task.setStatus(PENDING);
        task.setTotalCount(targetIds.size());
        task.setSuccessCount(0);
        task.setSkippedCount(0);
        task.setCreateBy(userId);
        try {
            ignore(() -> taskMapper.insert(task));
        } catch (DuplicateKeyException e) {
            existing = taskByRequest(userId, request.getRequestId());
            if (existing != null) return toVo(existing);
            throw new ServiceException("已有投保单导出任务正在处理，请等待完成后再创建");
        }
        submit(task.getId());
        return toVo(task);
    }

    public TableDataInfo<InsuranceApplicationExportTaskVo> list(PageQuery pageQuery) {
        Long userId = LoginHelper.getUserId();
        return ignore(() -> {
            Page<InsuranceApplicationExportTask> page = pageQuery.build();
            taskMapper.selectPage(page, Wrappers.<InsuranceApplicationExportTask>lambdaQuery()
                .eq(InsuranceApplicationExportTask::getTenantId, PLATFORM_TENANT)
                .eq(InsuranceApplicationExportTask::getCreateBy, userId)
                .orderByDesc(InsuranceApplicationExportTask::getId));
            return TableDataInfo.build(page.convert(this::toVo));
        });
    }

    public InsuranceApplicationExportTaskVo get(Long taskId) {
        return toVo(ownedTask(taskId));
    }

    public ExportFile file(Long taskId) {
        InsuranceApplicationExportTask task = ownedTask(taskId);
        if (!List.of(SUCCESS, PARTIAL).contains(task.getStatus()) || task.getZipKey() == null) {
            throw new ServiceException("导出文件尚未生成");
        }
        if (task.getExpiresAt() == null || !task.getExpiresAt().after(new Date())) {
            expire(task);
            throw new ServiceException("导出文件已过期，请重新创建任务");
        }
        return new ExportFile(task.getFileName(), task.getZipSize(), task.getStorageConfig(), task.getZipKey());
    }

    public void write(ExportFile file, OutputStream out) {
        storage.writeExport(file.storageConfig(), file.key(), out, properties.getMaxBytes());
    }

    private List<Long> resolveTargetIds(InsuranceApplicationExportRequest request) {
        List<Long> targetIds;
        if ("SELECTED".equals(request.getScope())) {
            if (request.getOrderIds() == null) return List.of();
            targetIds = request.getOrderIds().stream().filter(Objects::nonNull).distinct().toList();
        } else {
            InsuranceApplyRecordBo query = Optional.ofNullable(request.getQuery()).orElseGet(InsuranceApplyRecordBo::new);
            targetIds = ignore(() -> proxyOrderService.queryList(query)).stream()
                .map(InsuranceApplyRecordVo::getId).filter(Objects::nonNull).distinct().toList();
        }
        return expandBatchMainIds(targetIds);
    }

    private List<Long> expandBatchMainIds(List<Long> targetIds) {
        if (targetIds.isEmpty()) return List.of();
        List<InsuranceApplyRecord> selected = ignore(() -> orderMapper.selectList(
            Wrappers.<InsuranceApplyRecord>lambdaQuery()
                .in(InsuranceApplyRecord::getId, targetIds)
                .eq(InsuranceApplyRecord::getInsureMode, 1)
                .select(InsuranceApplyRecord::getId, InsuranceApplyRecord::getTenantId,
                    InsuranceApplyRecord::getOrderNo, InsuranceApplyRecord::getIsBatch)));
        Map<Long, InsuranceApplyRecord> selectedById = new HashMap<>();
        selected.forEach(order -> selectedById.put(order.getId(), order));

        List<InsuranceApplyRecord> batchMains = selected.stream()
            .filter(order -> Objects.equals(order.getIsBatch(), 1))
            .toList();
        Map<String, List<Long>> childIdsByBatch = new HashMap<>();
        if (!batchMains.isEmpty()) {
            List<String> tenantIds = batchMains.stream().map(InsuranceApplyRecord::getTenantId)
                .filter(Objects::nonNull).distinct().toList();
            List<String> batchOrderNos = batchMains.stream().map(InsuranceApplyRecord::getOrderNo)
                .filter(Objects::nonNull).distinct().toList();
            if (!tenantIds.isEmpty() && !batchOrderNos.isEmpty()) {
                List<InsuranceApplyRecord> children = ignore(() -> orderMapper.selectList(
                    Wrappers.<InsuranceApplyRecord>lambdaQuery()
                        .in(InsuranceApplyRecord::getTenantId, tenantIds)
                        .in(InsuranceApplyRecord::getBatchOrderNo, batchOrderNos)
                        .eq(InsuranceApplyRecord::getIsBatch, 2)
                        .eq(InsuranceApplyRecord::getInsureMode, 1)
                        .orderByAsc(InsuranceApplyRecord::getId)
                        .select(InsuranceApplyRecord::getId, InsuranceApplyRecord::getTenantId,
                            InsuranceApplyRecord::getBatchOrderNo)));
                for (InsuranceApplyRecord child : children) {
                    childIdsByBatch.computeIfAbsent(platformKey(child.getTenantId(), child.getBatchOrderNo()),
                        ignored -> new ArrayList<>()).add(child.getId());
                }
            }
        }

        LinkedHashSet<Long> expanded = new LinkedHashSet<>();
        for (Long targetId : targetIds) {
            InsuranceApplyRecord order = selectedById.get(targetId);
            if (order != null && Objects.equals(order.getIsBatch(), 1)) {
                List<Long> childIds = childIdsByBatch.getOrDefault(
                    platformKey(order.getTenantId(), order.getOrderNo()), List.of());
                if (!childIds.isEmpty()) {
                    expanded.addAll(childIds);
                    continue;
                }
            }
            expanded.add(targetId);
        }
        return List.copyOf(expanded);
    }

    private String platformKey(String tenantId, String orderNo) {
        return tenantId + "\u0000" + orderNo;
    }

    private void submit(Long taskId) {
        try {
            executor.execute(() -> runTask(taskId));
        } catch (RuntimeException e) {
            log.warn("投保单导出任务进入等待队列失败 taskId={}", taskId, e);
        }
    }

    private void runTask(Long taskId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        if (!claim(taskId, token)) return;
        Path zipFile = null;
        String uploadedKey = null;
        String uploadedStorageConfig = null;
        try {
            InsuranceApplicationExportTask task = task(taskId);
            List<Long> ids = decodeIds(task.getTargetIdsJson());
            List<ExportItem> results = new ArrayList<>(ids.size());
            List<LinkedHashMap<String, Object>> informationRows = new ArrayList<>();
            Map<String, LinkedHashMap<String, String>> applicationFieldCache = new HashMap<>();
            Set<String> zipEntryNames = new HashSet<>();
            zipFile = Files.createTempFile("application-form-export-" + taskId + "-", ".zip");
            long sourceBytes = 0;
            boolean capacityReached = false;
            try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(zipFile), StandardCharsets.UTF_8)) {
                for (int index = 0; index < ids.size(); index++) {
                    heartbeat(taskId, token);
                    Long orderId = ids.get(index);
                    InsuranceApplyRecordVo order = ignore(() -> proxyOrderService.queryById(orderId));
                    if (order == null) {
                        results.add(ExportItem.skipped(index + 1, orderId, "MISSING", "代投保订单不存在"));
                        continue;
                    }
                    if (capacityReached) {
                        results.add(ExportItem.skipped(index + 1, order, "达到单任务容量上限"));
                        continue;
                    }
                    try {
                        Map<String, Object> status = applicationFormService.platformStatus(orderId);
                        String formStatus = Objects.toString(status.get("status"), "MISSING");
                        if (!Boolean.TRUE.equals(status.get("required"))) {
                            results.add(ExportItem.skipped(index + 1, order, "该订单未启用签字投保单"));
                            continue;
                        }
                        if (!"READY".equals(formStatus)) {
                            results.add(ExportItem.skipped(index + 1, order, formStatus, statusReason(formStatus)));
                            continue;
                        }
                        InsuranceApplicationFormService.PlatformArchive archive = applicationFormService.platformArchive(orderId);
                        byte[] pdf = archive.bytes();
                        if (sourceBytes + pdf.length + MANIFEST_RESERVE_BYTES > properties.getMaxBytes()) {
                            capacityReached = true;
                            results.add(ExportItem.skipped(index + 1, order, formStatus, "达到单任务容量上限"));
                            continue;
                        }
                        String policyNo = findPolicyNo(order);
                        String applicantName = firstNotBlank(
                            Objects.toString(archive.snapshot().get("applicantName"), ""),
                            archive.applicant() == null ? null : archive.applicant().getApplicantName(),
                            order.getCustomerName());
                        String insuredName = firstNotBlank(
                            Objects.toString(archive.snapshot().get("insuredName"), ""),
                            archive.insured() == null ? null : archive.insured().getInsuredName(),
                            order.getCustomerName());
                        String number = firstNotBlank(policyNo, order.getOrderNo());
                        String pdfName = uniqueZipEntry("投保单/" + applicationPdfName(number, applicantName, insuredName), zipEntryNames);
                        LinkedHashMap<String, Object> informationRow = buildInformationRow(
                            informationRows.size() + 1, order, policyNo, archive, applicationFieldCache);
                        zip.putNextEntry(new ZipEntry(pdfName));
                        zip.write(pdf);
                        zip.closeEntry();
                        sourceBytes += pdf.length;
                        informationRows.add(informationRow);
                        results.add(ExportItem.success(index + 1, order, formStatus, pdfName));
                    } catch (ServiceException e) {
                        results.add(ExportItem.skipped(index + 1, order, safeMessage(e)));
                    } catch (RuntimeException e) {
                        log.warn("读取投保单失败 taskId={} orderId={} errorType={}", taskId, orderId, e.getClass().getSimpleName());
                        results.add(ExportItem.skipped(index + 1, order, "文件读取失败"));
                    }
                }
                byte[] workbook = buildInformationWorkbook(informationRows);
                byte[] manifest = buildManifest(results);
                if (sourceBytes + workbook.length + manifest.length > properties.getMaxBytes()) {
                    throw new ServiceException("投保单批量导出文件超过1GB限制");
                }
                zip.putNextEntry(new ZipEntry("投保信息表.xlsx"));
                zip.write(workbook);
                zip.closeEntry();
                zip.putNextEntry(new ZipEntry("导出结果清单.txt"));
                zip.write(manifest);
                zip.closeEntry();
            }
            long zipSize = Files.size(zipFile);
            if (zipSize > properties.getMaxBytes()) throw new ServiceException("投保单批量导出文件超过1GB限制");
            int successCount = (int) results.stream().filter(ExportItem::success).count();
            int skippedCount = results.size() - successCount;
            String storageConfig = storage.config();
            uploadedStorageConfig = storageConfig;
            String fileName = "投保单批量导出_" + FILE_TIME.format(LocalDateTime.now()) + "_" + taskId + ".zip";
            uploadedKey = "insurance-application-exports/" + LocalDateTime.now().toLocalDate() + "/" + taskId + "_" + token + ".zip";
            String zipHash = sha256(zipFile);
            storage.putExport(storageConfig, uploadedKey, zipFile);
            zipFile = null;
            String finalUploadedKey = uploadedKey;
            Date now = new Date();
            Date expiresAt = Date.from(now.toInstant().plus(properties.getRetentionHours(), java.time.temporal.ChronoUnit.HOURS));
            String finalStatus = skippedCount == 0 ? SUCCESS : PARTIAL;
            int updated = ignore(() -> taskMapper.update(null, Wrappers.<InsuranceApplicationExportTask>lambdaUpdate()
                .eq(InsuranceApplicationExportTask::getId, taskId)
                .eq(InsuranceApplicationExportTask::getWorkerToken, token)
                .eq(InsuranceApplicationExportTask::getStatus, RUNNING)
                .set(InsuranceApplicationExportTask::getStatus, finalStatus)
                .set(InsuranceApplicationExportTask::getSuccessCount, successCount)
                .set(InsuranceApplicationExportTask::getSkippedCount, skippedCount)
                .set(InsuranceApplicationExportTask::getResultJson, encode(results))
                .set(InsuranceApplicationExportTask::getFileName, fileName)
                .set(InsuranceApplicationExportTask::getZipKey, finalUploadedKey)
                .set(InsuranceApplicationExportTask::getZipHash, zipHash)
                .set(InsuranceApplicationExportTask::getZipSize, zipSize)
                .set(InsuranceApplicationExportTask::getStorageConfig, storageConfig)
                .set(InsuranceApplicationExportTask::getExpiresAt, expiresAt)
                .set(InsuranceApplicationExportTask::getFinishedAt, now)
                .set(InsuranceApplicationExportTask::getHeartbeatTime, now)
                .set(InsuranceApplicationExportTask::getActiveUserId, null)
                .set(InsuranceApplicationExportTask::getWorkerToken, null)));
            if (updated == 0) {
                storage.deleteExport(storageConfig, finalUploadedKey);
            } else {
                log.info("投保单批量导出完成 taskId={} status={} total={} success={} skipped={} size={}",
                    taskId, finalStatus, results.size(), successCount, skippedCount, zipSize);
            }
        } catch (Exception e) {
            log.error("投保单批量导出失败 taskId={} reason={}", taskId, safeMessage(e));
            fail(taskId, token, safeMessage(e));
            if (uploadedKey != null && uploadedStorageConfig != null) {
                try {
                    storage.deleteExport(uploadedStorageConfig, uploadedKey);
                } catch (Exception cleanupError) {
                    log.warn("清理失败的投保单导出文件异常 taskId={} errorType={}", taskId, cleanupError.getClass().getSimpleName());
                }
            }
        } finally {
            if (zipFile != null) {
                try {
                    Files.deleteIfExists(zipFile);
                } catch (IOException e) {
                    log.warn("删除投保单导出临时文件失败 taskId={} path={}", taskId, zipFile, e);
                }
            }
        }
    }

    private boolean claim(Long taskId, String token) {
        Date now = new Date();
        Date stale = Date.from(now.toInstant().minus(5, java.time.temporal.ChronoUnit.MINUTES));
        int rows = ignore(() -> taskMapper.update(null, Wrappers.<InsuranceApplicationExportTask>lambdaUpdate()
            .eq(InsuranceApplicationExportTask::getId, taskId)
            .and(w -> w.eq(InsuranceApplicationExportTask::getStatus, PENDING)
                .or(x -> x.eq(InsuranceApplicationExportTask::getStatus, RUNNING)
                    .lt(InsuranceApplicationExportTask::getHeartbeatTime, stale)))
            .set(InsuranceApplicationExportTask::getStatus, RUNNING)
            .set(InsuranceApplicationExportTask::getWorkerToken, token)
            .set(InsuranceApplicationExportTask::getStartedAt, now)
            .set(InsuranceApplicationExportTask::getHeartbeatTime, now)
            .set(InsuranceApplicationExportTask::getFailureReason, null)));
        return rows == 1;
    }

    private void heartbeat(Long taskId, String token) {
        int rows = ignore(() -> taskMapper.update(null, Wrappers.<InsuranceApplicationExportTask>lambdaUpdate()
            .eq(InsuranceApplicationExportTask::getId, taskId)
            .eq(InsuranceApplicationExportTask::getWorkerToken, token)
            .eq(InsuranceApplicationExportTask::getStatus, RUNNING)
            .set(InsuranceApplicationExportTask::getHeartbeatTime, new Date())));
        if (rows == 0) throw new ServiceException("导出任务执行权已失效");
    }

    private void fail(Long taskId, String token, String reason) {
        ignore(() -> taskMapper.update(null, Wrappers.<InsuranceApplicationExportTask>lambdaUpdate()
            .eq(InsuranceApplicationExportTask::getId, taskId)
            .eq(InsuranceApplicationExportTask::getWorkerToken, token)
            .eq(InsuranceApplicationExportTask::getStatus, RUNNING)
            .set(InsuranceApplicationExportTask::getStatus, FAILED)
            .set(InsuranceApplicationExportTask::getFailureReason, reason)
            .set(InsuranceApplicationExportTask::getFinishedAt, new Date())
            .set(InsuranceApplicationExportTask::getActiveUserId, null)
            .set(InsuranceApplicationExportTask::getWorkerToken, null)));
    }

    private void scanSafely() {
        try {
            expireCompletedTasks();
            Date stale = Date.from(new Date().toInstant().minus(5, java.time.temporal.ChronoUnit.MINUTES));
            List<InsuranceApplicationExportTask> waiting = ignore(() -> taskMapper.selectList(
                Wrappers.<InsuranceApplicationExportTask>lambdaQuery()
                    .and(w -> w.eq(InsuranceApplicationExportTask::getStatus, PENDING)
                        .or(x -> x.eq(InsuranceApplicationExportTask::getStatus, RUNNING)
                            .lt(InsuranceApplicationExportTask::getHeartbeatTime, stale)))
                    .orderByAsc(InsuranceApplicationExportTask::getId)
                    .last("LIMIT 20")));
            waiting.forEach(item -> submit(item.getId()));
        } catch (Exception e) {
            log.error("扫描投保单批量导出任务失败 errorType={}", e.getClass().getSimpleName());
        }
    }

    private void expireCompletedTasks() {
        Date now = new Date();
        List<InsuranceApplicationExportTask> expired = ignore(() -> taskMapper.selectList(
            Wrappers.<InsuranceApplicationExportTask>lambdaQuery()
                .in(InsuranceApplicationExportTask::getStatus, SUCCESS, PARTIAL)
                .le(InsuranceApplicationExportTask::getExpiresAt, now)
                .isNotNull(InsuranceApplicationExportTask::getZipKey)
                .last("LIMIT 20")));
        for (InsuranceApplicationExportTask task : expired) expire(task);
    }

    private void expire(InsuranceApplicationExportTask task) {
        if (task.getZipKey() != null && task.getStorageConfig() != null) {
            storage.deleteExport(task.getStorageConfig(), task.getZipKey());
        }
        ignore(() -> taskMapper.update(null, Wrappers.<InsuranceApplicationExportTask>lambdaUpdate()
            .eq(InsuranceApplicationExportTask::getId, task.getId())
            .in(InsuranceApplicationExportTask::getStatus, SUCCESS, PARTIAL)
            .set(InsuranceApplicationExportTask::getStatus, EXPIRED)
            .set(InsuranceApplicationExportTask::getZipKey, null)));
    }

    private InsuranceApplicationExportTask ownedTask(Long taskId) {
        Long userId = LoginHelper.getUserId();
        InsuranceApplicationExportTask task = ignore(() -> taskMapper.selectOne(
            Wrappers.<InsuranceApplicationExportTask>lambdaQuery()
                .eq(InsuranceApplicationExportTask::getId, taskId)
                .eq(InsuranceApplicationExportTask::getTenantId, PLATFORM_TENANT)
                .eq(InsuranceApplicationExportTask::getCreateBy, userId)));
        if (task == null) throw new ServiceException("导出任务不存在或无权访问");
        return task;
    }

    private InsuranceApplicationExportTask task(Long taskId) {
        InsuranceApplicationExportTask task = ignore(() -> taskMapper.selectById(taskId));
        if (task == null) throw new ServiceException("导出任务不存在");
        return task;
    }

    private InsuranceApplicationExportTask taskByRequest(Long userId, String requestId) {
        return ignore(() -> taskMapper.selectOne(Wrappers.<InsuranceApplicationExportTask>lambdaQuery()
            .eq(InsuranceApplicationExportTask::getTenantId, PLATFORM_TENANT)
            .eq(InsuranceApplicationExportTask::getCreateBy, userId)
            .eq(InsuranceApplicationExportTask::getRequestId, requestId)));
    }

    private InsuranceApplicationExportTaskVo toVo(InsuranceApplicationExportTask task) {
        InsuranceApplicationExportTaskVo vo = new InsuranceApplicationExportTaskVo();
        vo.setId(task.getId());
        vo.setScope(task.getScope());
        vo.setStatus(task.getStatus());
        vo.setTotalCount(task.getTotalCount());
        vo.setSuccessCount(task.getSuccessCount());
        vo.setSkippedCount(task.getSkippedCount());
        vo.setFileName(task.getFileName());
        vo.setZipSize(task.getZipSize());
        vo.setExpiresAt(task.getExpiresAt());
        vo.setStartedAt(task.getStartedAt());
        vo.setFinishedAt(task.getFinishedAt());
        vo.setFailureReason(task.getFailureReason());
        vo.setCreateTime(task.getCreateTime());
        vo.setCanDownload(List.of(SUCCESS, PARTIAL).contains(task.getStatus()) && task.getZipKey() != null
            && task.getExpiresAt() != null && task.getExpiresAt().after(new Date()));
        return vo;
    }

    private String findPolicyNo(InsuranceApplyRecordVo order) {
        InsurancePolicy policy = ignore(() -> policyMapper.selectOne(Wrappers.<InsurancePolicy>lambdaQuery()
            .eq(InsurancePolicy::getTenantId, order.getTenantId())
            .eq(InsurancePolicy::getOrderNo, order.getOrderNo())
            .isNotNull(InsurancePolicy::getPolicyNo)
            .ne(InsurancePolicy::getPolicyNo, "")
            .orderByDesc(InsurancePolicy::getId)
            .select(InsurancePolicy::getPolicyNo)
            .last("LIMIT 1")));
        return policy == null ? "" : Objects.toString(policy.getPolicyNo(), "");
    }

    private LinkedHashMap<String, Object> buildInformationRow(
        int index, InsuranceApplyRecordVo order, String policyNo,
        InsuranceApplicationFormService.PlatformArchive archive,
        Map<String, LinkedHashMap<String, String>> applicationFieldCache) {
        LinkedHashMap<String, Object> row = new LinkedHashMap<>();
        var applicant = archive.applicant();
        var insured = archive.insured();
        Map<String, Object> snapshot = archive.snapshot();
        row.put("序号", index);
        row.put("订单号", order.getOrderNo());
        row.put("保单号", policyNo);
        row.put("产品名称", order.getProductName());
        row.put("起保日期", formatDate(order.getPolicyStartDate()));
        row.put("投保人姓名", firstNotBlank(Objects.toString(snapshot.get("applicantName"), ""),
            applicant == null ? null : applicant.getApplicantName()));
        row.put("投保人证件类型", firstNotBlank(Objects.toString(snapshot.get("applicantCertTypeLabel"), ""),
            applicant == null ? null : applicant.getApplicantCertType()));
        row.put("投保人证件号", firstNotBlank(Objects.toString(snapshot.get("applicantCertNo"), ""),
            applicant == null ? null : applicant.getApplicantCertNo()));
        row.put("投保人证件生效期", applicant == null ? "" : applicant.getCertStartDate());
        row.put("投保人证件到期日", applicant == null ? "" : applicant.getCertEndDate());
        row.put("投保人手机号", firstNotBlank(Objects.toString(snapshot.get("applicantPhone"), ""),
            applicant == null ? null : applicant.getApplicantPhone()));
        row.put("投保人地址", firstNotBlank(Objects.toString(snapshot.get("applicantAddress"), ""),
            applicant == null ? null : applicant.getApplicantAddress()));
        row.put("与投保人关系", firstNotBlank(Objects.toString(snapshot.get("relationLabel"), ""),
            insured == null ? null : insured.getRelation()));
        row.put("被保人姓名", firstNotBlank(Objects.toString(snapshot.get("insuredName"), ""),
            insured == null ? null : insured.getInsuredName()));
        row.put("被保人证件类型", firstNotBlank(Objects.toString(snapshot.get("insuredCertTypeLabel"), ""),
            insured == null ? null : insured.getInsuredCertType()));
        row.put("被保人证件号", firstNotBlank(Objects.toString(snapshot.get("insuredCertNo"), ""),
            insured == null ? null : insured.getInsuredCertNo()));
        row.put("被保人证件生效期", insured == null ? "" : insured.getCertStartDate());
        row.put("被保人证件到期日", insured == null ? "" : insured.getCertEndDate());
        row.put("被保人手机号", firstNotBlank(Objects.toString(snapshot.get("insuredPhone"), ""),
            insured == null ? null : insured.getInsuredPhone()));
        row.put("被保人地址", firstNotBlank(Objects.toString(snapshot.get("insuredAddress"), ""),
            insured == null ? null : insured.getInsuredAddress()));

        Map<String, Object> extras = decodeExtras(order.getInsureExtraData());
        Map<String, Object> applicationForm = mapValue(extras.get("applicationForm"));
        LinkedHashMap<String, String> fieldLabels = applicationFieldLabels(archive, applicationFieldCache);
        LinkedHashSet<String> applicationKeys = new LinkedHashSet<>(fieldLabels.keySet());
        applicationKeys.addAll(applicationForm.keySet());
        for (String key : applicationKeys) {
            String label = fieldLabels.getOrDefault(key, key);
            Object value = snapshot.containsKey(key) ? snapshot.get(key) : applicationForm.get(key);
            row.put("投保单-" + label + "(" + key + ")", excelValue(value));
        }
        for (Map.Entry<String, Object> entry : extras.entrySet()) {
            if (!"applicationForm".equals(entry.getKey())) {
                row.put("扩展字段-" + entry.getKey(), excelValue(entry.getValue()));
            }
        }
        return row;
    }

    private LinkedHashMap<String, String> applicationFieldLabels(
        InsuranceApplicationFormService.PlatformArchive archive,
        Map<String, LinkedHashMap<String, String>> cache) {
        String cacheKey = archive.templateCode() + "\u0000" + archive.templateVersion();
        return cache.computeIfAbsent(cacheKey, ignored -> {
            LinkedHashMap<String, String> labels = new LinkedHashMap<>();
            try {
                for (JsonNode field : applicationFormService.metadata(archive.templateCode(), archive.templateVersion()).path("fields")) {
                    String key = field.path("key").asText();
                    if (!key.isBlank()) labels.put(key, field.path("label").asText(key));
                }
            } catch (RuntimeException e) {
                log.warn("读取归档投保单字段名称失败 templateCode={} templateVersion={} errorType={}",
                    archive.templateCode(), archive.templateVersion(), e.getClass().getSimpleName());
            }
            return labels;
        });
    }

    private Map<String, Object> decodeExtras(String value) {
        if (value == null || value.isBlank()) return Map.of();
        try {
            return json.readValue(value, new TypeReference<LinkedHashMap<String, Object>>() { });
        } catch (Exception e) {
            log.warn("读取投保扩展信息失败 errorType={}", e.getClass().getSimpleName());
            return Map.of();
        }
    }

    private static Map<String, Object> mapValue(Object value) {
        if (!(value instanceof Map<?, ?> map)) return Map.of();
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, item) -> result.put(Objects.toString(key, ""), item));
        return result;
    }

    private Object excelValue(Object value) {
        if (value == null) return "";
        if (value instanceof Map<?, ?> || value instanceof Collection<?> || value.getClass().isArray()) {
            try {
                return json.writeValueAsString(value);
            } catch (Exception e) {
                return Objects.toString(value, "");
            }
        }
        return value;
    }

    static byte[] buildInformationWorkbook(List<LinkedHashMap<String, Object>> rows) {
        LinkedHashSet<String> headers = new LinkedHashSet<>(INFORMATION_HEADERS);
        rows.forEach(row -> headers.addAll(row.keySet()));
        List<List<String>> head = headers.stream().map(List::of).toList();
        List<List<Object>> data = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            data.add(headers.stream().map(key -> row.getOrDefault(key, "")).toList());
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ExcelWriter writer = FastExcel.write(output).autoCloseStream(false).build()) {
            WriteSheet sheet = FastExcel.writerSheet("投保信息").head(head).build();
            writer.write(data, sheet);
        }
        return output.toByteArray();
    }

    static String applicationPdfName(String number, String applicantName, String insuredName) {
        return safePart(number) + "_" + safePart(applicantName) + "_" + safePart(insuredName) + "_投保单.pdf";
    }

    private static String uniqueZipEntry(String path, Set<String> used) {
        if (used.add(path)) return path;
        int dot = path.lastIndexOf('.');
        String base = dot < 0 ? path : path.substring(0, dot);
        String extension = dot < 0 ? "" : path.substring(dot);
        int number = 2;
        String candidate;
        do {
            candidate = base + "_(" + number++ + ")" + extension;
        } while (!used.add(candidate));
        return candidate;
    }

    private static String firstNotBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return "";
    }

    private static String formatDate(Date value) {
        return value == null ? "" : DATE.format(value.toInstant().atZone(BUSINESS_ZONE));
    }

    static byte[] buildManifest(List<ExportItem> results) {
        StringBuilder text = new StringBuilder();
        text.append("序号\t租户编号\t租户名称\t订单主键\t订单号\t产品名称\t投保单状态\t导出结果\tPDF文件名\t跳过或失败原因\r\n");
        for (ExportItem item : results) {
            text.append(item.index()).append('\t')
                .append(cell(item.tenantId())).append('\t')
                .append(cell(item.tenantName())).append('\t')
                .append(cell(item.orderId())).append('\t')
                .append(cell(item.orderNo())).append('\t')
                .append(cell(item.productName())).append('\t')
                .append(cell(item.formStatus())).append('\t')
                .append(item.success() ? "成功" : "跳过").append('\t')
                .append(cell(item.pdfName())).append('\t')
                .append(cell(item.reason())).append("\r\n");
        }
        byte[] body = text.toString().getBytes(StandardCharsets.UTF_8);
        byte[] withBom = new byte[body.length + 3];
        withBom[0] = (byte) 0xEF;
        withBom[1] = (byte) 0xBB;
        withBom[2] = (byte) 0xBF;
        System.arraycopy(body, 0, withBom, 3, body.length);
        return withBom;
    }

    static String safePart(String value) {
        String safe = Objects.toString(value, "unknown").replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
        return safe.isEmpty() ? "unknown" : safe.substring(0, Math.min(safe.length(), 100));
    }

    private static String cell(Object value) {
        return Objects.toString(value, "").replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
    }

    private static String statusReason(String status) {
        return switch (status) {
            case "DRAFT" -> "等待签署";
            case "GENERATING" -> "正在生成投保单";
            case "FAILED" -> "投保单生成失败";
            case "INVALID" -> "投保资料已变更，原投保单已失效";
            default -> "尚未生成投保单";
        };
    }

    private static String safeMessage(Throwable error) {
        String message = error instanceof ServiceException ? error.getMessage() : "导出任务执行失败";
        if (message == null || message.isBlank()) message = "导出任务执行失败";
        return message.substring(0, Math.min(message.length(), 200));
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (var input = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) digest.update(buffer, 0, read);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private String encode(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (Exception e) {
            throw new ServiceException("导出任务数据格式错误");
        }
    }

    private List<Long> decodeIds(String value) {
        try {
            return json.readValue(value, new TypeReference<List<Long>>() { });
        } catch (Exception e) {
            throw new ServiceException("导出任务订单数据损坏");
        }
    }

    private <T> T ignore(java.util.function.Supplier<T> action) {
        return TenantHelper.ignore(action);
    }

    public record ExportFile(String fileName, long size, String storageConfig, String key) {
    }

    record ExportItem(int index, String tenantId, String tenantName, Long orderId, String orderNo,
                      String productName, String formStatus, boolean success, String pdfName, String reason) {
        static ExportItem success(int index, InsuranceApplyRecordVo order, String status, String pdfName) {
            return new ExportItem(index, order.getTenantId(), order.getTenantName(), order.getId(), order.getOrderNo(),
                order.getProductName(), status, true, pdfName, "");
        }

        static ExportItem skipped(int index, InsuranceApplyRecordVo order, String reason) {
            return skipped(index, order, Objects.toString(order.getApplicationFormStatus(), "MISSING"), reason);
        }

        static ExportItem skipped(int index, InsuranceApplyRecordVo order, String status, String reason) {
            return new ExportItem(index, order.getTenantId(), order.getTenantName(), order.getId(), order.getOrderNo(),
                order.getProductName(), status, false, "", reason);
        }

        static ExportItem skipped(int index, Long orderId, String status, String reason) {
            return new ExportItem(index, "", "", orderId, "", "", status, false, "", reason);
        }
    }
}
