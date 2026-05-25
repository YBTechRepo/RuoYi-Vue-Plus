package org.dromara.insurance.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.dromara.commission.domain.BizCommissionRecord;
import org.dromara.commission.mapper.BizCommissionRecordMapper;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.tenant.helper.TenantHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.dromara.insurance.domain.bo.InsuranceApplyRecordBo;
import org.dromara.insurance.domain.bo.InsuranceProductSaveBo;
import org.dromara.insurance.domain.vo.InsuranceApplyRecordVo;
import org.dromara.insurance.domain.InsuranceApplyRecord;
import org.dromara.insurance.domain.InsuranceOrderApplicant;
import org.dromara.insurance.domain.InsuranceOrderInsured;
import org.dromara.insurance.mapper.InsuranceApplyRecordMapper;
import org.dromara.insurance.mapper.InsuranceOrderApplicantMapper;
import org.dromara.insurance.mapper.InsuranceOrderInsuredMapper;
import org.dromara.insurance.service.IInsuranceProductConfigService;
import org.dromara.insurance.service.IInsuranceProxyOrderService;
import org.dromara.insurance.utils.DynamicInsureFieldUtils;
import org.dromara.system.domain.vo.SysDictDataVo;
import org.dromara.system.domain.vo.SysTenantVo;
import org.dromara.system.service.ISysDictTypeService;
import org.dromara.system.service.ISysTenantService;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;

/**
 * 代投保订单查询Service业务层处理 (Admin全局视图)
 *
 * @author li.xiang
 * @date 2026-04-14
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InsuranceProxyOrderServiceImpl implements IInsuranceProxyOrderService {

    private final InsuranceApplyRecordMapper baseMapper;
    private final InsuranceOrderApplicantMapper applicantMapper;
    private final InsuranceOrderInsuredMapper insuredMapper;
    private final BizCommissionRecordMapper commissionRecordMapper;
    private final ISysTenantService sysTenantService;
    private final IInsuranceProductConfigService productConfigService;
    private final ISysDictTypeService sysDictTypeService;

    /**
     * 查询代投保订单查询
     *
     * @param id 主键
     * @return 代投保订单查询
     */
    @Override
    public InsuranceApplyRecordVo queryById(Long id){
        return TenantHelper.ignore(() -> {
            InsuranceApplyRecordVo vo = baseMapper.selectVoOne(new LambdaQueryWrapper<InsuranceApplyRecord>()
                .eq(InsuranceApplyRecord::getId, id)
                .eq(InsuranceApplyRecord::getInsureMode, 1));
            fillTenantName(vo);
            return vo;
        });
    }

    /**
     * 分页查询代投保订单查询列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 代投保订单查询分页列表
     */
    @Override
    public TableDataInfo<InsuranceApplyRecordVo> queryPageList(InsuranceApplyRecordBo bo, PageQuery pageQuery) {
        return TenantHelper.ignore(() -> {
            LambdaQueryWrapper<InsuranceApplyRecord> lqw = buildQueryWrapper(bo);
            Page<InsuranceApplyRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
            fillTenantNames(result.getRecords());
            return TableDataInfo.build(result);
        });
    }

    /**
     * 查询符合条件的代投保订单查询列表
     *
     * @param bo 查询条件
     * @return 代投保订单查询列表
     */
    @Override
    public List<InsuranceApplyRecordVo> queryList(InsuranceApplyRecordBo bo) {
        return TenantHelper.ignore(() -> {
            LambdaQueryWrapper<InsuranceApplyRecord> lqw = buildQueryWrapper(bo);
            List<InsuranceApplyRecordVo> list = baseMapper.selectVoList(lqw);
            fillTenantNames(list);
            return list;
        });
    }

    @Override
    public void exportList(InsuranceApplyRecordBo bo, HttpServletResponse response) {
        TenantHelper.ignore(() -> {
            List<InsuranceApplyRecordVo> list = queryExportRecords(bo);
            exportDynamicExcel(list, response);
            return null;
        });
    }

    private List<InsuranceApplyRecordVo> queryExportRecords(InsuranceApplyRecordBo bo) {
        InsuranceApplyRecordBo queryBo = Optional.ofNullable(bo).orElseGet(InsuranceApplyRecordBo::new);
        Map<String, Object> params = queryBo.getParams();
        LambdaQueryWrapper<InsuranceApplyRecord> lqw = Wrappers.lambdaQuery();

        // 核心强制过滤：仅代投保模式
        lqw.eq(InsuranceApplyRecord::getInsureMode, 1);
        // 导出场景过滤：仅普通单(0)和批量子单(2)
        lqw.in(InsuranceApplyRecord::getIsBatch, Arrays.asList(0, 2));
        // 导出场景过滤：状态仅为已支付(0)
        lqw.eq(InsuranceApplyRecord::getStatus, 0);

        lqw.orderByDesc(InsuranceApplyRecord::getId);
        lqw.eq(StringUtils.isNotBlank(queryBo.getOrderNo()), InsuranceApplyRecord::getOrderNo, queryBo.getOrderNo());
        lqw.eq(StringUtils.isNotBlank(queryBo.getProductCode()), InsuranceApplyRecord::getProductCode, queryBo.getProductCode());
        lqw.like(StringUtils.isNotBlank(queryBo.getProductName()), InsuranceApplyRecord::getProductName, queryBo.getProductName());
        lqw.like(StringUtils.isNotBlank(queryBo.getAgentName()), InsuranceApplyRecord::getAgentName, queryBo.getAgentName());
        lqw.like(StringUtils.isNotBlank(queryBo.getCustomerName()), InsuranceApplyRecord::getCustomerName, queryBo.getCustomerName());
        lqw.eq(StringUtils.isNotBlank(queryBo.getCustomerMobile()), InsuranceApplyRecord::getCustomerMobile, queryBo.getCustomerMobile());
        lqw.eq(queryBo.getCommissionStatus() != null, InsuranceApplyRecord::getCommissionStatus, queryBo.getCommissionStatus());

        if (params != null && params.get("beginTime") != null && params.get("endTime") != null) {
            lqw.between(InsuranceApplyRecord::getCreateTime, params.get("beginTime"), params.get("endTime") + " 23:59:59");
        }

        List<InsuranceApplyRecordVo> list = baseMapper.selectVoList(lqw);
        fillTenantNames(list);
        fillPersonInfo(list);
        return list;
    }

    private void fillPersonInfo(List<InsuranceApplyRecordVo> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (InsuranceApplyRecordVo vo : list) {
            InsuranceOrderApplicant applicant = applicantMapper.selectOne(new LambdaQueryWrapper<InsuranceOrderApplicant>()
                .eq(InsuranceOrderApplicant::getOrderNo, vo.getOrderNo()));
            if (applicant != null) {
                vo.setAppName(applicant.getApplicantName());
                vo.setAppCertType(applicant.getApplicantCertType());
                vo.setAppCertNo(applicant.getApplicantCertNo());
                vo.setAppPhone(applicant.getApplicantPhone());
                vo.setAppAddress(applicant.getApplicantAddress());
            }

            InsuranceOrderInsured insured = insuredMapper.selectOne(new LambdaQueryWrapper<InsuranceOrderInsured>()
                .eq(InsuranceOrderInsured::getOrderNo, vo.getOrderNo()));
            if (insured != null) {
                vo.setRelation(insured.getRelation());
                vo.setInsuredName(insured.getInsuredName());
                vo.setInsuredCertType(insured.getInsuredCertType());
                vo.setInsuredCertNo(insured.getInsuredCertNo());
                vo.setInsuredPhone(insured.getInsuredPhone());
                vo.setInsuredAddress(insured.getInsuredAddress());
            }
        }
    }

    private void exportDynamicExcel(List<InsuranceApplyRecordVo> list, HttpServletResponse response) {
        List<InsuranceApplyRecordVo> exportList = Optional.ofNullable(list).orElseGet(Collections::emptyList);
        List<List<String>> summaryHead = toExcelHead(buildFixedExportHead());
        List<List<Object>> summaryRows = exportList.stream().map(this::buildFixedExportRow).toList();

        try {
            String fileName = URLEncoder.encode("代投保订单_多产品.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

            try (ExcelWriter writer = FastExcel.write(response.getOutputStream()).autoCloseStream(false).build()) {
                WriteSheet summarySheet = FastExcel.writerSheet(0, "订单汇总").head(summaryHead).build();
                writer.write(summaryRows, summarySheet);

                Map<Long, List<InsuranceApplyRecordVo>> productOrderMap = new LinkedHashMap<>();
                for (InsuranceApplyRecordVo record : exportList) {
                    if (record.getProductId() == null) {
                        continue;
                    }
                    productOrderMap.computeIfAbsent(record.getProductId(), key -> new ArrayList<>()).add(record);
                }

                Map<Long, List<DynamicInsureFieldUtils.Field>> productFieldCache = new HashMap<>();
                Map<String, Integer> sheetNameCounter = new HashMap<>();
                int sheetIndex = 1;
                for (Map.Entry<Long, List<InsuranceApplyRecordVo>> entry : productOrderMap.entrySet()) {
                    List<DynamicInsureFieldUtils.Field> fields = productFieldCache.computeIfAbsent(entry.getKey(), this::queryProductDynamicFields);
                    if (CollUtil.isEmpty(fields)) {
                        continue;
                    }
                    String productName = entry.getValue().stream()
                        .map(InsuranceApplyRecordVo::getProductName)
                        .filter(StringUtils::isNotBlank)
                        .findFirst()
                        .orElse("产品" + entry.getKey());
                    List<List<String>> head = buildProductExtraHead(fields);
                    List<List<Object>> rows = buildProductExtraRows(entry.getValue(), fields);
                    WriteSheet sheet = FastExcel.writerSheet(sheetIndex++, uniqueSheetName(productName + "-扩展字段", sheetNameCounter)).head(head).build();
                    writer.write(rows, sheet);
                }
            }
        } catch (IOException e) {
            log.error("导出代投保订单 Excel IO 异常", e);
            throw new ServiceException("导出Excel异常");
        }
    }

    private List<String> buildFixedExportHead() {
        return Arrays.asList(
            "订单号", "产品编码", "产品名称", "业务员姓名", "客户姓名", "客户手机号", "保单保费", "订单状态", "创建时间",
            "净费出单保费", "投保模式", "产品模式", "支付模式", "是否批量单", "所属批次单号",
            "投保人姓名", "投保人证件类型", "投保人证件号", "投保人手机号", "投保人地址",
            "被保人关系", "被保人姓名", "被保人证件类型", "被保人证件号", "被保人手机号", "被保人地址"
        );
    }

    private List<Object> buildFixedExportRow(InsuranceApplyRecordVo record) {
        List<Object> row = new ArrayList<>();
        row.add(record.getOrderNo());
        row.add(record.getProductCode());
        row.add(record.getProductName());
        row.add(record.getAgentName());
        row.add(record.getCustomerName());
        row.add(record.getCustomerMobile());
        row.add(record.getPremium());
        row.add(translateDict("insurance_apply_status", record.getStatus()));
        row.add(formatDate(record.getCreateTime()));
        row.add(record.getNetPremium());
        row.add(translateDict("insurance_product_insure_mode", record.getInsureMode()));
        row.add(translateDict("insurance_product_mode", record.getProductMode()));
        row.add(translateDict("insurance_product_payment_mode", record.getPaymentMode()));
        row.add(record.getIsBatch());
        row.add(record.getBatchOrderNo());
        row.add(record.getAppName());
        row.add(translateDict("insurance_id_type", record.getAppCertType()));
        row.add(record.getAppCertNo());
        row.add(record.getAppPhone());
        row.add(record.getAppAddress());
        row.add(translateDict("insurance_relationship_to_insured", record.getRelation()));
        row.add(record.getInsuredName());
        row.add(translateDict("insurance_id_type", record.getInsuredCertType()));
        row.add(record.getInsuredCertNo());
        row.add(record.getInsuredPhone());
        row.add(record.getInsuredAddress());
        return row;
    }

    private List<List<String>> buildProductExtraHead(List<DynamicInsureFieldUtils.Field> fields) {
        List<List<String>> head = toExcelHead(Arrays.asList("订单号", "产品名称", "客户姓名", "客户手机号", "被保人姓名"));
        fields.forEach(field -> head.add(Collections.singletonList(field.getLabel())));
        return head;
    }

    private List<List<Object>> buildProductExtraRows(List<InsuranceApplyRecordVo> records, List<DynamicInsureFieldUtils.Field> fields) {
        List<List<Object>> rows = new ArrayList<>();
        for (InsuranceApplyRecordVo record : records) {
            List<Object> row = new ArrayList<>();
            row.add(record.getOrderNo());
            row.add(record.getProductName());
            row.add(record.getCustomerName());
            row.add(record.getCustomerMobile());
            row.add(record.getInsuredName());
            Map<String, Object> extraData = StringUtils.isBlank(record.getInsureExtraData()) ? Collections.emptyMap() : JsonUtils.parseMap(record.getInsureExtraData());
            for (DynamicInsureFieldUtils.Field field : fields) {
                row.add(DynamicInsureFieldUtils.formatValue(field, extraData.get(field.getKey())));
            }
            rows.add(row);
        }
        return rows;
    }

    private List<List<String>> toExcelHead(List<String> heads) {
        return heads.stream().map(Collections::singletonList).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private List<DynamicInsureFieldUtils.Field> queryProductDynamicFields(Long productId) {
        if (productId == null) {
            return Collections.emptyList();
        }
        try {
            InsuranceProductSaveBo productData = productConfigService.getProductFull(productId);
            String schemaJson = productData == null || productData.getProduct() == null ? null : productData.getProduct().getInsureFormSchema();
            return DynamicInsureFieldUtils.parseSchema(schemaJson);
        } catch (Exception e) {
            log.warn("产品 {} 查询投保扩展字段失败，导出将跳过该产品扩展字段", productId, e);
            return Collections.emptyList();
        }
    }

    private String uniqueSheetName(String rawName, Map<String, Integer> sheetNameCounter) {
        String baseName = sanitizeSheetName(rawName);
        int count = sheetNameCounter.getOrDefault(baseName, 0) + 1;
        sheetNameCounter.put(baseName, count);
        if (count == 1) {
            return baseName;
        }
        String suffix = "_" + count;
        int maxBaseLength = Math.max(1, 31 - suffix.length());
        return (baseName.length() > maxBaseLength ? baseName.substring(0, maxBaseLength) : baseName) + suffix;
    }

    private String sanitizeSheetName(String rawName) {
        String name = StringUtils.isBlank(rawName) ? "扩展字段" : rawName.replaceAll("[\\\\/:*?\\[\\]]", " ").trim();
        if (StringUtils.isBlank(name)) {
            name = "扩展字段";
        }
        return name.length() > 31 ? name.substring(0, 31) : name;
    }

    private String formatDate(java.util.Date date) {
        return date == null ? "--" : DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
    }

    private String display(Object value, String defaultValue) {
        return value == null || StringUtils.isBlank(String.valueOf(value)) ? defaultValue : String.valueOf(value);
    }

    private String translateDict(String dictType, Object dictValue) {
        if (StringUtils.isBlank(dictType) || dictValue == null || StringUtils.isBlank(String.valueOf(dictValue))) {
            return display(dictValue, "--");
        }
        try {
            List<SysDictDataVo> dictDataList = sysDictTypeService.selectDictDataByType(dictType);
            if (CollUtil.isNotEmpty(dictDataList)) {
                String value = String.valueOf(dictValue);
                return dictDataList.stream()
                    .filter(item -> StringUtils.equals(item.getDictValue(), value))
                    .map(SysDictDataVo::getDictLabel)
                    .findFirst()
                    .orElse(value);
            }
        } catch (Exception e) {
            log.warn("字典 {} 的值 {} 翻译失败，将使用原始值", dictType, dictValue, e);
        }
        return String.valueOf(dictValue);
    }
    @Override
    public List<Map<String, Object>> querySubOrders(String batchOrderNo) {
        return TenantHelper.ignore(() -> {
            List<Map<String, Object>> result = new ArrayList<>();
            if (StringUtils.isBlank(batchOrderNo)) {
                return result;
            }

            List<InsuranceApplyRecord> subOrders = baseMapper.selectList(new LambdaQueryWrapper<InsuranceApplyRecord>()
                .eq(InsuranceApplyRecord::getBatchOrderNo, batchOrderNo)
                .eq(InsuranceApplyRecord::getIsBatch, 2)
                .orderByAsc(InsuranceApplyRecord::getId));

            for (InsuranceApplyRecord subOrder : subOrders) {
                Map<String, Object> map = new HashMap<>();
                map.put("orderNo", subOrder.getOrderNo());
                map.put("status", subOrder.getStatus());

                InsuranceOrderApplicant applicant = applicantMapper.selectOne(new LambdaQueryWrapper<InsuranceOrderApplicant>()
                    .eq(InsuranceOrderApplicant::getOrderNo, subOrder.getOrderNo()));
                map.put("appName", applicant != null ? applicant.getApplicantName() : "");

                InsuranceOrderInsured insured = insuredMapper.selectOne(new LambdaQueryWrapper<InsuranceOrderInsured>()
                    .eq(InsuranceOrderInsured::getOrderNo, subOrder.getOrderNo()));
                map.put("insuredName", insured != null ? insured.getInsuredName() : "");

                result.add(map);
            }
            return result;
        });
    }

    @Override
    public Map<String, Object> queryPersonDetail(String orderNo) {
        return TenantHelper.ignore(() -> {
            Map<String, Object> result = new HashMap<>();
            if (StringUtils.isBlank(orderNo)) {
                return result;
            }

            result.put("orderNo", orderNo);

            InsuranceApplyRecord record = baseMapper.selectOne(new LambdaQueryWrapper<InsuranceApplyRecord>()
                .eq(InsuranceApplyRecord::getOrderNo, orderNo)
                .eq(InsuranceApplyRecord::getInsureMode, 1));
            if (record != null) {
                result.put("productId", record.getProductId());
                result.put("productName", record.getProductName());
                result.put("insureExtraData", record.getInsureExtraData());
                try {
                    InsuranceProductSaveBo productData = productConfigService.getProductFull(record.getProductId());
                    String schemaJson = productData == null || productData.getProduct() == null ? null : productData.getProduct().getInsureFormSchema();
                    result.put("insureFormSchema", schemaJson);
                } catch (Exception e) {
                    log.warn("订单 {} 查询投保扩展字段配置失败", orderNo, e);
                }
            }

            InsuranceOrderApplicant applicant = applicantMapper.selectOne(new LambdaQueryWrapper<InsuranceOrderApplicant>()
                .eq(InsuranceOrderApplicant::getOrderNo, orderNo));
            if (applicant != null) {
                result.put("appName", applicant.getApplicantName());
                result.put("appPhone", applicant.getApplicantPhone());
                result.put("appCertType", applicant.getApplicantCertType());
                result.put("appCertNo", applicant.getApplicantCertNo());
                result.put("appAddress", applicant.getApplicantAddress());
            }

            InsuranceOrderInsured insured = insuredMapper.selectOne(new LambdaQueryWrapper<InsuranceOrderInsured>()
                .eq(InsuranceOrderInsured::getOrderNo, orderNo));
            if (insured != null) {
                result.put("insuredName", insured.getInsuredName());
                result.put("insuredPhone", insured.getInsuredPhone());
                result.put("insuredCertType", insured.getInsuredCertType());
                result.put("insuredCertNo", insured.getInsuredCertNo());
                result.put("relation", insured.getRelation());
                result.put("insuredAddress", insured.getInsuredAddress());
            }

            return result;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean changeStatus(InsuranceApplyRecordBo bo) {
        if (bo == null || bo.getId() == null || StringUtils.isBlank(bo.getOrderNo())) {
            throw new ServiceException("参数错误，无法取消订单");
        }
        if (!Integer.valueOf(4).equals(bo.getStatus())) {
            throw new ServiceException("代投保订单仅允许变更为已取消");
        }

        return TenantHelper.ignore(() -> {
            InsuranceApplyRecord order = baseMapper.selectOne(new LambdaQueryWrapper<InsuranceApplyRecord>()
                .eq(InsuranceApplyRecord::getId, bo.getId())
                .eq(InsuranceApplyRecord::getOrderNo, bo.getOrderNo())
                .eq(InsuranceApplyRecord::getInsureMode, 1));
            if (order == null) {
                throw new ServiceException("订单不存在");
            }

            List<InsuranceApplyRecord> targetOrders = new ArrayList<>();
            targetOrders.add(order);
            if (Integer.valueOf(1).equals(order.getIsBatch())) {
                List<InsuranceApplyRecord> subOrders = baseMapper.selectList(new LambdaQueryWrapper<InsuranceApplyRecord>()
                    .eq(InsuranceApplyRecord::getBatchOrderNo, order.getOrderNo())
                    .eq(InsuranceApplyRecord::getIsBatch, 2)
                    .eq(InsuranceApplyRecord::getInsureMode, 1));
                targetOrders.addAll(subOrders);
            }

            List<String> orderNos = new ArrayList<>();
            Set<Long> applyRecordIds = new HashSet<>();
            for (InsuranceApplyRecord targetOrder : targetOrders) {
                if (targetOrder != null && StringUtils.isNotBlank(targetOrder.getOrderNo())) {
                    orderNos.add(targetOrder.getOrderNo());
                }
                if (targetOrder != null && targetOrder.getId() != null) {
                    applyRecordIds.add(targetOrder.getId());
                }
            }

            int applyRows = baseMapper.update(null, new LambdaUpdateWrapper<InsuranceApplyRecord>()
                .in(InsuranceApplyRecord::getOrderNo, orderNos)
                .eq(InsuranceApplyRecord::getInsureMode, 1)
                .set(InsuranceApplyRecord::getStatus, 4)
                .set(InsuranceApplyRecord::getDelFlag, "2"));
            if (applyRows <= 0) {
                throw new ServiceException("取消订单失败");
            }

            Set<Long> policyIds = new HashSet<>(applyRecordIds);
            Set<String> policyNos = new HashSet<>(orderNos);
            if (policyIds.isEmpty() && policyNos.isEmpty()) {
                return true;
            }

            LambdaUpdateWrapper<BizCommissionRecord> commissionUpdate = new LambdaUpdateWrapper<BizCommissionRecord>()
                .set(BizCommissionRecord::getStatus, 1)
                .set(BizCommissionRecord::getDelFlag, "2");
            commissionUpdate.and(wrapper -> {
                if (!policyIds.isEmpty()) {
                    wrapper.in(BizCommissionRecord::getPolicyId, policyIds);
                }
                if (!policyIds.isEmpty() && !policyNos.isEmpty()) {
                    wrapper.or();
                }
                if (!policyNos.isEmpty()) {
                    wrapper.in(BizCommissionRecord::getPolicyNo, policyNos);
                }
            });
            commissionRecordMapper.update(null, commissionUpdate);
            return true;
        });
    }

    private LambdaQueryWrapper<InsuranceApplyRecord> buildQueryWrapper(InsuranceApplyRecordBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<InsuranceApplyRecord> lqw = Wrappers.lambdaQuery();

        // 🌟 核心强制过滤：仅代投保模式
        lqw.eq(InsuranceApplyRecord::getInsureMode, 1);
        // 🌟 核心强制过滤：仅显示普通单和批次主单
        lqw.in(InsuranceApplyRecord::getIsBatch, Arrays.asList(0, 1));

        lqw.orderByDesc(InsuranceApplyRecord::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNo()), InsuranceApplyRecord::getOrderNo, bo.getOrderNo());
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), InsuranceApplyRecord::getProductCode, bo.getProductCode());
        lqw.like(StringUtils.isNotBlank(bo.getProductName()), InsuranceApplyRecord::getProductName, bo.getProductName());
        lqw.like(StringUtils.isNotBlank(bo.getAgentName()), InsuranceApplyRecord::getAgentName, bo.getAgentName());
        lqw.like(StringUtils.isNotBlank(bo.getCustomerName()), InsuranceApplyRecord::getCustomerName, bo.getCustomerName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustomerMobile()), InsuranceApplyRecord::getCustomerMobile, bo.getCustomerMobile());
        //lqw.eq(bo.getStatus() != null, InsuranceApplyRecord::getStatus, bo.getStatus());
        lqw.eq(InsuranceApplyRecord::getStatus,0);
        lqw.eq(bo.getCommissionStatus() != null, InsuranceApplyRecord::getCommissionStatus, bo.getCommissionStatus());

        if (params.get("beginTime") != null && params.get("endTime") != null) {
            lqw.between(InsuranceApplyRecord::getCreateTime, params.get("beginTime"), params.get("endTime") + " 23:59:59");
        }

        return lqw;
    }

    /**
     * 补充租户名称
     */
    private void fillTenantName(InsuranceApplyRecordVo vo) {
        if (vo == null || StringUtils.isBlank(vo.getTenantId())) {
            return;
        }
        SysTenantVo tenant = sysTenantService.queryByTenantId(vo.getTenantId());
        if (tenant != null) {
            vo.setTenantName(tenant.getCompanyName());
        }
    }

    /**
     * 批量补充租户名称
     */
    private void fillTenantNames(List<InsuranceApplyRecordVo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<String, String> tenantNameMap = new HashMap<>();
        for (InsuranceApplyRecordVo vo : list) {
            if (vo == null || StringUtils.isBlank(vo.getTenantId()) || tenantNameMap.containsKey(vo.getTenantId())) {
                continue;
            }
            SysTenantVo tenant = sysTenantService.queryByTenantId(vo.getTenantId());
            tenantNameMap.put(vo.getTenantId(), tenant != null ? tenant.getCompanyName() : "");
        }
        for (InsuranceApplyRecordVo vo : list) {
            if (vo != null && StringUtils.isNotBlank(vo.getTenantId())) {
                vo.setTenantName(tenantNameMap.get(vo.getTenantId()));
            }
        }
    }

    /**
     * 新增代投保订单查询
     *
     * @param bo 代投保订单查询
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(InsuranceApplyRecordBo bo) {
        InsuranceApplyRecord add = MapstructUtils.convert(bo, InsuranceApplyRecord.class);
        // 🌟 强制设为代投保模式
        add.setInsureMode(1);
        validEntityBeforeSave(add);
        return TenantHelper.ignore(() -> {
            boolean flag = baseMapper.insert(add) > 0;
            if (flag) {
                bo.setId(add.getId());
            }
            return flag;
        });
    }

    /**
     * 修改代投保订单查询
     *
     * @param bo 代投保订单查询
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(InsuranceApplyRecordBo bo) {
        InsuranceApplyRecord update = MapstructUtils.convert(bo, InsuranceApplyRecord.class);
        validEntityBeforeSave(update);
        // 🌟 仅允许修改代投保模式的订单
        return TenantHelper.ignore(() -> baseMapper.update(update, new LambdaUpdateWrapper<InsuranceApplyRecord>()
            .eq(InsuranceApplyRecord::getId, bo.getId())
            .eq(InsuranceApplyRecord::getInsureMode, 1)) > 0);
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(InsuranceApplyRecord entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除代投保订单查询信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        // 🌟 仅允许删除代投保模式的订单
        return TenantHelper.ignore(() -> baseMapper.delete(new LambdaQueryWrapper<InsuranceApplyRecord>()
            .in(InsuranceApplyRecord::getId, ids)
            .eq(InsuranceApplyRecord::getInsureMode, 1)) > 0);
    }
}
