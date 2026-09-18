package org.dromara.insurance.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdcardUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.finance.service.IBizUserAccountService;
import org.dromara.insurance.domain.InsuranceApplicationDocument;
import org.dromara.insurance.domain.InsuranceApplicationSignInvite;
import org.dromara.insurance.domain.InsuranceApplyRecord;
import org.dromara.insurance.domain.InsuranceOrderApplicant;
import org.dromara.insurance.domain.InsuranceOrderInsured;
import org.dromara.insurance.domain.dto.BatchSubmitDTO;
import org.dromara.insurance.domain.dto.SignedBatchInsuredImportDto;
import org.dromara.insurance.domain.vo.InsuranceSalesProductVo;
import org.dromara.insurance.mapper.InsuranceApplicationDocumentMapper;
import org.dromara.insurance.mapper.InsuranceApplyRecordMapper;
import org.dromara.insurance.mapper.InsuranceOrderApplicantMapper;
import org.dromara.insurance.mapper.InsuranceOrderInsuredMapper;
import org.dromara.insurance.utils.DynamicInsureFieldUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 需要签字投保单产品的批量创建、进度和整批扣款。 */
@Service
@RequiredArgsConstructor
public class SignedBatchInsuranceService {
    private static final int MAX_BATCH_SIZE = 50;
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

    private final SignedBatchTemplateDescriptor templateDescriptor;
    private final ApplicationFormTemplate applicationFormTemplate;
    private final ApplicationFormGuard applicationFormGuard;
    private final InsuranceApplicationFormService applicationFormService;
    private final IInsuranceProductConfigService productConfigService;
    private final IInsuranceApplyRecordService applyRecordService;
    private final InsuranceApplyRecordMapper orders;
    private final InsuranceOrderApplicantMapper applicants;
    private final InsuranceOrderInsuredMapper insureds;
    private final InsuranceApplicationDocumentMapper documents;
    private final IBizUserAccountService userAccountService;
    private final InsurancePublicSignService publicSignService;

    public void validateSubmission(BatchSubmitDTO submit) {
        if (submit == null || submit.getProductId() == null) throw new ServiceException("产品不能为空");
        SignedBatchTemplateDescriptor.Descriptor descriptor = templateDescriptor.describe(submit.getProductId());
        if (!SignedBatchTemplateDescriptor.MODE.equals(submit.getTemplateMode())) {
            throw new ServiceException("模板模式不匹配，请重新上传该产品的签字批量模板");
        }
        if (!SignedBatchTemplateDescriptor.VERSION.equals(submit.getTemplateVersion())
            || !descriptor.getSchemaHash().equals(submit.getSchemaHash())) {
            throw new ServiceException("批量模板版本或字段配置已变化，请重新下载模板");
        }
        if (CollUtil.isEmpty(submit.getSignedAuditList())) throw new ServiceException("投保人员名单不能为空");
        if (submit.getSignedAuditList().size() > MAX_BATCH_SIZE) throw new ServiceException("签字产品每批最多50人");
        parseStartDate(submit.getPolicyStartDate());

        int row = 1;
        for (SignedBatchInsuredImportDto item : submit.getSignedAuditList()) {
            normalizeRow(item);
            validateRow(item, descriptor, row++);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String createPendingBatch(BatchSubmitDTO submit) {
        validateSubmission(submit);
        SignedBatchTemplateDescriptor.Descriptor descriptor = templateDescriptor.describe(submit.getProductId());
        InsuranceSalesProductVo product = productConfigService.querySalesProductById(submit.getProductId());
        if (product == null) throw new ServiceException("产品不存在或已下架");
        Date startDate = parseStartDate(submit.getPolicyStartDate());

        BigDecimal gross = product.getMinPremium() == null ? BigDecimal.ZERO : product.getMinPremium();
        BigDecimal rate = product.getDisplayCommissionRate() == null ? BigDecimal.ZERO : product.getDisplayCommissionRate();
        BigDecimal net = gross.multiply(BigDecimal.ONE.subtract(rate)).setScale(2, RoundingMode.HALF_UP);
        int count = submit.getSignedAuditList().size();
        String batchNo = "BH" + DateUtil.format(new Date(), "yyyyMMddHHmmss")
            + cn.hutool.core.util.RandomUtil.randomNumbers(4);

        InsuranceApplyRecord main = baseOrder(batchNo, product, startDate);
        main.setPremium(gross.multiply(BigDecimal.valueOf(count)).setScale(2, RoundingMode.HALF_UP));
        main.setNetPremium(net.multiply(BigDecimal.valueOf(count)).setScale(2, RoundingMode.HALF_UP));
        main.setCustomerName(submit.getSignedAuditList().get(0).getName() + "等" + count + "人");
        main.setCustomerMobile(submit.getSignedAuditList().get(0).getPhone());
        main.setStatus(3);
        main.setIsBatch(1);
        main.setApplicationFormRequired(false);
        main.setInsureExtraData(JsonUtils.toJsonString(Map.of("batchTemplate", Map.of(
            "mode", SignedBatchTemplateDescriptor.MODE,
            "version", SignedBatchTemplateDescriptor.VERSION,
            "schemaHash", descriptor.getSchemaHash(),
            "applicationTemplateCode", descriptor.getProduct().getApplicationTemplateCode(),
            "applicationTemplateVersion", descriptor.getProduct().getApplicationTemplateVersion()))));
        orders.insert(main);

        int index = 1;
        for (SignedBatchInsuredImportDto dto : submit.getSignedAuditList()) {
            String subNo = batchNo + "-" + String.format("%04d", index++);
            InsuranceApplyRecord sub = baseOrder(subNo, product, startDate);
            sub.setBatchOrderNo(batchNo);
            sub.setIsBatch(2);
            sub.setStatus(3);
            sub.setPremium(gross);
            sub.setNetPremium(net);
            sub.setCustomerName(dto.getName());
            sub.setCustomerMobile(dto.getPhone());
            sub.setApplicationFormRequired(true);
            Map<String, Object> extra = new LinkedHashMap<>(dto.getExtraData());
            extra.put("applicationForm", new LinkedHashMap<>(dto.getApplicationForm()));
            sub.setInsureExtraData(JsonUtils.toJsonString(extra));
            orders.insert(sub);
            savePeople(subNo, dto);
            applicationFormService.prepare(subNo);
            InsuranceApplicationDocument document = applicationFormGuard.latest(subNo);
            InsuranceOrderApplicant applicant = applicants.selectOne(new LambdaQueryWrapper<InsuranceOrderApplicant>()
                .eq(InsuranceOrderApplicant::getOrderNo, subNo));
            InsuranceOrderInsured insured = insureds.selectOne(new LambdaQueryWrapper<InsuranceOrderInsured>()
                .eq(InsuranceOrderInsured::getOrderNo, subNo));
            publicSignService.createInvites(sub, document, applicant, insured);
        }
        return batchNo;
    }

    public Map<String, Object> progress(String batchOrderNo) {
        InsuranceApplyRecord main = ownedMain(batchOrderNo, false);
        List<InsuranceApplyRecord> children = childOrders(batchOrderNo);
        List<String> orderNos = children.stream().map(InsuranceApplyRecord::getOrderNo).toList();
        Map<String, InsuranceApplicationDocument> latest = new LinkedHashMap<>();
        if (!orderNos.isEmpty()) {
            for (InsuranceApplicationDocument document : documents.selectList(
                new LambdaQueryWrapper<InsuranceApplicationDocument>()
                    .in(InsuranceApplicationDocument::getOrderNo, orderNos)
                    .orderByDesc(InsuranceApplicationDocument::getId))) {
                latest.putIfAbsent(document.getOrderNo(), document);
            }
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        int ready = 0;
        for (InsuranceApplyRecord child : children) {
            InsuranceApplicationDocument doc = latest.get(child.getOrderNo());
            String status = doc == null ? "MISSING" : doc.getStatus();
            if ("READY".equals(status)) ready++;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("orderNo", child.getOrderNo());
            row.put("customerName", child.getCustomerName());
            row.put("customerMobile", child.getCustomerMobile());
            row.put("applicationFormStatus", status);
            row.put("documentId", doc == null ? null : doc.getId().toString());
            row.put("invites", publicSignService.list(batchOrderNo, child.getOrderNo()));
            rows.add(row);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("batchOrderNo", main.getOrderNo());
        result.put("status", main.getStatus());
        result.put("totalCount", children.size());
        result.put("readyCount", ready);
        result.put("allReady", !children.isEmpty() && ready == children.size());
        result.put("totalAmount", main.getNetPremium());
        result.put("rows", rows);
        return result;
    }

    public Map<String, Object> issueLink(String batchOrderNo, Long inviteId) {
        ownedMain(batchOrderNo, false);
        InsuranceApplicationSignInvite invite = publicSignService.findOwnedInvite(batchOrderNo, inviteId);
        InsuranceApplyRecord child = orders.selectOne(new LambdaQueryWrapper<InsuranceApplyRecord>()
            .eq(InsuranceApplyRecord::getOrderNo, invite.getOrderNo())
            .eq(InsuranceApplyRecord::getBatchOrderNo, batchOrderNo)
            .eq(InsuranceApplyRecord::getIsBatch, 2)
            .eq(InsuranceApplyRecord::getStatus, 3));
        if (child == null) throw new ServiceException("签署子单不存在");
        return publicSignService.issueLink(inviteId, child);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> issueAllLinks(String batchOrderNo) {
        ownedMain(batchOrderNo, false);
        Map<String, InsuranceApplyRecord> children = new LinkedHashMap<>();
        for (InsuranceApplyRecord child : childOrders(batchOrderNo)) {
            if (Objects.equals(child.getStatus(), 3)) children.put(child.getOrderNo(), child);
        }

        List<Map<String, Object>> links = new ArrayList<>();
        for (InsuranceApplicationSignInvite invite : publicSignService.findOwnedInvites(batchOrderNo)) {
            if ("SIGNED".equals(invite.getStatus())) continue;
            InsuranceApplyRecord child = children.get(invite.getOrderNo());
            if (child == null) throw new ServiceException("签署子单不存在或状态已变化");
            Map<String, Object> issued = publicSignService.issueLink(invite.getId(), child);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("inviteId", invite.getId().toString());
            row.put("orderNo", invite.getOrderNo());
            row.put("customerName", child.getCustomerName());
            row.put("signerName", invite.getSignerName());
            row.put("signerRole", invite.getSignerRole());
            row.put("url", issued.get("url"));
            row.put("expiresAt", issued.get("expiresAt"));
            links.add(row);
        }
        if (links.isEmpty()) throw new ServiceException("当前没有待签署链接");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", links.size());
        result.put("links", links);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void pay(String batchOrderNo) {
        InsuranceApplyRecord main = ownedMain(batchOrderNo, true);
        if (Objects.equals(main.getStatus(), 0)) return;
        if (!Objects.equals(main.getStatus(), 3)) throw new ServiceException("该批次当前状态不能支付");
        assertSignedBatchSnapshot(main);

        List<InsuranceApplyRecord> children = childOrders(batchOrderNo);
        if (children.isEmpty()) throw new ServiceException("批次没有子单");
        BigDecimal totalNet = BigDecimal.ZERO;
        BigDecimal totalGross = BigDecimal.ZERO;
        for (InsuranceApplyRecord child : children) {
            applicationFormGuard.assertReady(child);
            totalNet = totalNet.add(Objects.requireNonNullElse(child.getNetPremium(), BigDecimal.ZERO));
            totalGross = totalGross.add(Objects.requireNonNullElse(child.getPremium(), BigDecimal.ZERO));
        }
        totalNet = totalNet.setScale(2, RoundingMode.HALF_UP);
        totalGross = totalGross.setScale(2, RoundingMode.HALF_UP);
        if (totalNet.compareTo(main.getNetPremium()) != 0 || totalGross.compareTo(main.getPremium()) != 0) {
            throw new ServiceException("批次金额已变化，请联系管理员核对");
        }

        userAccountService.deductForOrder(main.getAgentUserId(), main.getOrderNo(), totalNet,
            "签字批量投保整批扣款：" + main.getOrderNo() + "_" + main.getProductName());
        Date paidAt = new Date();
        orders.update(null, Wrappers.<InsuranceApplyRecord>lambdaUpdate()
            .eq(InsuranceApplyRecord::getBatchOrderNo, batchOrderNo)
            .eq(InsuranceApplyRecord::getStatus, 3)
            .set(InsuranceApplyRecord::getStatus, 0)
            .set(InsuranceApplyRecord::getPayTime, paidAt));
        orders.update(null, Wrappers.<InsuranceApplyRecord>lambdaUpdate()
            .eq(InsuranceApplyRecord::getId, main.getId())
            .eq(InsuranceApplyRecord::getStatus, 3)
            .set(InsuranceApplyRecord::getPayTime, paidAt));
        // 主单状态由现有支付成功入口 CAS 更新，并且只发布一次主单佣金事件。
        applyRecordService.handleOrderPaySuccess(main.getId());
    }

    private InsuranceApplyRecord baseOrder(String orderNo, InsuranceSalesProductVo product, Date startDate) {
        InsuranceApplyRecord order = new InsuranceApplyRecord();
        order.setOrderNo(orderNo);
        order.setProductId(product.getId());
        order.setProductCode(product.getProductCode());
        order.setProductName(product.getProductName());
        order.setAgentUserId(LoginHelper.getUserId());
        order.setAgentName(LoginHelper.getUsername());
        order.setAgentDeptId(LoginHelper.getDeptId());
        order.setPolicyStartDate(startDate);
        order.setInsureMode(1);
        order.setProductMode(1);
        order.setPaymentMode(1);
        return order;
    }

    private void savePeople(String orderNo, SignedBatchInsuredImportDto dto) {
        InsuranceOrderApplicant applicant = new InsuranceOrderApplicant();
        applicant.setOrderNo(orderNo);
        applicant.setApplicantName(dto.getAppName());
        applicant.setApplicantCertType(dto.getAppCertType());
        applicant.setApplicantCertNo(dto.getAppCertNo());
        applicant.setCertStartDate(dto.getAppCertStartDate());
        applicant.setCertEndDate(dto.getAppCertEndDate());
        applicant.setApplicantPhone(dto.getAppPhone());
        applicant.setApplicantAddress(joinAddress(dto.getAppRegion(), dto.getAppAddress()));
        applicants.insert(applicant);

        InsuranceOrderInsured insured = new InsuranceOrderInsured();
        insured.setOrderNo(orderNo);
        insured.setRelation(dto.getRelation());
        insured.setInsuredName(dto.getName());
        insured.setInsuredCertType(dto.getCertType());
        insured.setInsuredCertNo(dto.getCertNo());
        insured.setCertStartDate(dto.getCertStartDate());
        insured.setCertEndDate(dto.getCertEndDate());
        insured.setInsuredPhone(dto.getPhone());
        insured.setInsuredAddress(joinAddress(dto.getRegion(), dto.getAddress()));
        insureds.insert(insured);
    }

    private void validateRow(SignedBatchInsuredImportDto item,
                             SignedBatchTemplateDescriptor.Descriptor descriptor, int row) {
        if (item == null) throw new ServiceException("第" + row + "行数据为空");
        List<String> missing = new ArrayList<>();
        if (StringUtils.isBlank(item.getAppName())) missing.add("投保人姓名");
        if (StringUtils.isBlank(item.getAppCertType())) missing.add("投保人证件类型");
        if (StringUtils.isBlank(item.getAppCertNo())) missing.add("投保人证件号码");
        if (StringUtils.isBlank(item.getAppCertStartDate())) missing.add("投保人证件生效期");
        if (StringUtils.isBlank(item.getAppCertEndDate())) missing.add("投保人证件到期日");
        if (StringUtils.isBlank(item.getAppPhone())) missing.add("投保人手机号码");
        if (StringUtils.isBlank(item.getAppRegion()) || StringUtils.isBlank(item.getAppAddress())) missing.add("投保人地址");
        if (StringUtils.isBlank(item.getRelation())) missing.add("与投保人关系");
        if (StringUtils.isBlank(item.getName()) || StringUtils.isBlank(item.getCertType())
            || StringUtils.isBlank(item.getCertNo())) missing.add("被保险人身份资料");
        if (StringUtils.isBlank(item.getCertStartDate()) || StringUtils.isBlank(item.getCertEndDate())) missing.add("被保险人证件有效期");
        if (StringUtils.isBlank(item.getPhone())) missing.add("被保险人手机号码");
        if (StringUtils.isBlank(item.getRegion()) || StringUtils.isBlank(item.getAddress())) missing.add("被保险人地址");
        if (!missing.isEmpty()) throw new ServiceException("第" + row + "行缺少：" + String.join("、", missing));
        if ("0".equals(item.getAppCertType()) && !IdcardUtil.isValidCard18(item.getAppCertNo()))
            throw new ServiceException("第" + row + "行投保人身份证号码格式错误");
        if ("0".equals(item.getCertType()) && !IdcardUtil.isValidCard18(item.getCertNo()))
            throw new ServiceException("第" + row + "行被保险人身份证号码格式错误");
        if (!item.getAppPhone().matches("^1[3-9]\\d{9}$") || !item.getPhone().matches("^1[3-9]\\d{9}$"))
            throw new ServiceException("第" + row + "行手机号码格式错误");
        validateDates(item.getAppCertStartDate(), item.getAppCertEndDate(), row, "投保人");
        validateDates(item.getCertStartDate(), item.getCertEndDate(), row, "被保险人");
        applicationFormTemplate.validateFields(item.getApplicationForm());
        for (DynamicInsureFieldUtils.Field field : descriptor.getDynamicFields()) {
            String error = DynamicInsureFieldUtils.validateValue(field, item.getExtraData().get(field.getKey()));
            if (StringUtils.isNotBlank(error)) throw new ServiceException("第" + row + "行" + error);
        }
    }

    private void normalizeRow(SignedBatchInsuredImportDto item) {
        if (item == null) return;
        if (item.getExtraData() == null) item.setExtraData(new LinkedHashMap<>());
        if (item.getApplicationForm() == null) item.setApplicationForm(new LinkedHashMap<>());
        if ("0".equals(item.getRelation())) {
            if (StringUtils.isBlank(item.getName())) item.setName(item.getAppName());
            if (StringUtils.isBlank(item.getCertType())) item.setCertType(item.getAppCertType());
            if (StringUtils.isBlank(item.getCertNo())) item.setCertNo(item.getAppCertNo());
            if (StringUtils.isBlank(item.getCertStartDate())) item.setCertStartDate(item.getAppCertStartDate());
            if (StringUtils.isBlank(item.getCertEndDate())) item.setCertEndDate(item.getAppCertEndDate());
            if (StringUtils.isBlank(item.getPhone())) item.setPhone(item.getAppPhone());
            if (StringUtils.isBlank(item.getRegion())) item.setRegion(item.getAppRegion());
            if (StringUtils.isBlank(item.getAddress())) item.setAddress(item.getAppAddress());
        }
        if ("0".equals(item.getCertType()) && IdcardUtil.isValidCard18(item.getCertNo())) {
            String birth = IdcardUtil.getBirthByIdCard(item.getCertNo());
            if (birth != null && birth.length() == 8) {
                birth = birth.substring(0, 4) + "-" + birth.substring(4, 6) + "-" + birth.substring(6);
            }
            item.getApplicationForm().put("insuredBirthday", birth);
            item.getApplicationForm().put("insuredGender", IdcardUtil.getGenderByIdCard(item.getCertNo()) == 1 ? "男" : "女");
        }
    }

    private void validateDates(String start, String end, int row, String person) {
        try {
            if (DateUtil.parse(start, "yyyy-MM-dd").after(DateUtil.parse(end, "yyyy-MM-dd")))
                throw new ServiceException("第" + row + "行" + person + "证件到期日不能早于生效期");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("第" + row + "行" + person + "证件日期格式应为yyyy-MM-dd");
        }
    }

    private Date parseStartDate(String value) {
        if (StringUtils.isBlank(value)) throw new ServiceException("起保日期不能为空");
        try {
            Date date = DateUtil.parseDate(value);
            LocalDate localDate = date.toInstant().atZone(BUSINESS_ZONE).toLocalDate();
            if (!localDate.isAfter(LocalDate.now(BUSINESS_ZONE))) throw new ServiceException("起保日期不能早于明天");
            return DateUtil.beginOfDay(date);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("起保日期格式不正确");
        }
    }

    private InsuranceApplyRecord ownedMain(String batchOrderNo, boolean lock) {
        LambdaQueryWrapper<InsuranceApplyRecord> query = new LambdaQueryWrapper<InsuranceApplyRecord>()
            .eq(InsuranceApplyRecord::getOrderNo, batchOrderNo)
            .eq(InsuranceApplyRecord::getIsBatch, 1);
        if (lock) query.last("FOR UPDATE");
        InsuranceApplyRecord main = orders.selectOne(query);
        if (main == null) throw new ServiceException("批次不存在");
        if (!Objects.equals(main.getAgentUserId(), LoginHelper.getUserId())) throw new ServiceException("无权操作该批次");
        return main;
    }

    private List<InsuranceApplyRecord> childOrders(String batchOrderNo) {
        return orders.selectList(new LambdaQueryWrapper<InsuranceApplyRecord>()
            .eq(InsuranceApplyRecord::getBatchOrderNo, batchOrderNo)
            .eq(InsuranceApplyRecord::getIsBatch, 2)
            .orderByAsc(InsuranceApplyRecord::getId));
    }

    private void assertSignedBatchSnapshot(InsuranceApplyRecord main) {
        try {
            Object mode = JsonUtils.parseMap(main.getInsureExtraData()).get("batchTemplate");
            if (!(mode instanceof Map<?, ?> metadata)
                || !SignedBatchTemplateDescriptor.MODE.equals(Objects.toString(metadata.get("mode"), ""))) {
                throw new ServiceException("该批次不是签字产品待签批次");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("批次模板快照无法校验");
        }
    }

    private String joinAddress(String region, String detail) {
        return (StringUtils.trimToEmpty(region) + " " + StringUtils.trimToEmpty(detail)).trim();
    }
}
