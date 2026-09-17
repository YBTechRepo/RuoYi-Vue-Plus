package org.dromara.insurance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.dromara.insurance.domain.InsuranceApplicationExportTask;
import org.dromara.insurance.domain.bo.InsuranceApplicationExportRequest;
import org.dromara.insurance.domain.bo.InsuranceApplyRecordBo;
import org.dromara.insurance.domain.vo.InsuranceApplicationExportTaskVo;
import org.dromara.insurance.domain.vo.InsuranceApplyRecordVo;
import org.dromara.insurance.mapper.InsuranceApplicationExportTaskMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
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

    private final InsuranceApplicationExportTaskMapper taskMapper;
    private final IInsuranceProxyOrderService proxyOrderService;
    private final InsuranceApplicationFormService applicationFormService;
    private final ApplicationFormStorage storage;
    private final ApplicationFormExportProperties properties;
    private final ObjectMapper json;
    private final ScheduledExecutorService scheduledExecutorService;
    private final TaskExecutor executor;

    public InsuranceApplicationExportService(InsuranceApplicationExportTaskMapper taskMapper,
                                             IInsuranceProxyOrderService proxyOrderService,
                                             InsuranceApplicationFormService applicationFormService,
                                             ApplicationFormStorage storage,
                                             ApplicationFormExportProperties properties,
                                             ObjectMapper json,
                                             ScheduledExecutorService scheduledExecutorService,
                                             @Qualifier("applicationFormExportExecutor") TaskExecutor executor) {
        this.taskMapper = taskMapper;
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
        if ("SELECTED".equals(request.getScope())) {
            if (request.getOrderIds() == null) return List.of();
            return request.getOrderIds().stream().filter(Objects::nonNull).distinct().toList();
        }
        InsuranceApplyRecordBo query = Optional.ofNullable(request.getQuery()).orElseGet(InsuranceApplyRecordBo::new);
        return ignore(() -> proxyOrderService.queryList(query)).stream()
            .map(InsuranceApplyRecordVo::getId).filter(Objects::nonNull).distinct().toList();
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
                        byte[] pdf = applicationFormService.platformPdf(orderId).bytes();
                        if (sourceBytes + pdf.length + MANIFEST_RESERVE_BYTES > properties.getMaxBytes()) {
                            capacityReached = true;
                            results.add(ExportItem.skipped(index + 1, order, formStatus, "达到单任务容量上限"));
                            continue;
                        }
                        String pdfName = "投保单/" + safePart(order.getTenantId()) + "_" + safePart(order.getOrderNo()) + "_" + orderId + "_投保单.pdf";
                        zip.putNextEntry(new ZipEntry(pdfName));
                        zip.write(pdf);
                        zip.closeEntry();
                        sourceBytes += pdf.length;
                        results.add(ExportItem.success(index + 1, order, formStatus, pdfName));
                    } catch (ServiceException e) {
                        results.add(ExportItem.skipped(index + 1, order, safeMessage(e)));
                    } catch (RuntimeException e) {
                        log.warn("读取投保单失败 taskId={} orderId={} errorType={}", taskId, orderId, e.getClass().getSimpleName());
                        results.add(ExportItem.skipped(index + 1, order, "文件读取失败"));
                    }
                }
                zip.putNextEntry(new ZipEntry("导出结果清单.txt"));
                zip.write(buildManifest(results));
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
