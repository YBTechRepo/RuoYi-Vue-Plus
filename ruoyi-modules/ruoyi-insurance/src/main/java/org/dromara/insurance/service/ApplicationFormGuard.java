package org.dromara.insurance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.insurance.domain.*;
import org.dromara.insurance.mapper.*;
import org.springframework.stereotype.Service;
import java.util.Objects;

/** 在订单事务内调用；锁顺序始终是订单、投保单。 */
@Service
@RequiredArgsConstructor
public class ApplicationFormGuard {
    private final InsuranceProductConfigMapper products;
    private final InsuranceApplyRecordMapper orders;
    private final InsuranceApplicationDocumentMapper documents;
    private final InsuranceOrderApplicantMapper applicants;
    private final InsuranceOrderInsuredMapper insureds;
    private final com.fasterxml.jackson.databind.ObjectMapper json;

    public String sourceHash(InsuranceApplyRecord order) {
        var a=applicants.selectOne(new LambdaQueryWrapper<InsuranceOrderApplicant>().eq(InsuranceOrderApplicant::getOrderNo,order.getOrderNo()));
        var list=insureds.selectList(new LambdaQueryWrapper<InsuranceOrderInsured>().eq(InsuranceOrderInsured::getOrderNo,order.getOrderNo()).orderByAsc(InsuranceOrderInsured::getId));
        java.util.List<Object> values=new java.util.ArrayList<>();
        values.add(order.getProductId());values.add(order.getPremium());values.add(order.getPolicyStartDate());values.add(order.getInsureExtraData());
        values.add(order.getPaymentMode());values.add(order.getProductMode());values.add(order.getInsureMode());
        if(a!=null)values.add(java.util.Arrays.asList(a.getApplicantName(),a.getApplicantPhone(),a.getApplicantAddress(),a.getApplicantCertNo(),a.getApplicantCertType(),a.getCertStartDate(),a.getCertEndDate()));
        for(var i:list)values.add(java.util.Arrays.asList(i.getInsuredName(),i.getInsuredPhone(),i.getInsuredAddress(),i.getInsuredCertNo(),i.getInsuredCertType(),i.getRelation(),i.getCertStartDate(),i.getCertEndDate()));
        try{return ApplicationFormTemplate.hash(json.writeValueAsBytes(values));}catch(Exception e){throw new ServiceException("投保资料摘要计算失败");}
    }

    public void enrich(java.util.List<org.dromara.insurance.domain.vo.InsuranceApplyRecordVo> rows) {
        if(rows.isEmpty())return;
        var nos=rows.stream().map(org.dromara.insurance.domain.vo.InsuranceApplyRecordVo::getOrderNo).toList();
        var docs=documents.selectList(new LambdaQueryWrapper<InsuranceApplicationDocument>().in(InsuranceApplicationDocument::getOrderNo,nos)
            .select(InsuranceApplicationDocument::getId,InsuranceApplicationDocument::getOrderNo,InsuranceApplicationDocument::getStatus)
            .orderByDesc(InsuranceApplicationDocument::getId));
        java.util.Map<String,String> statuses=new java.util.HashMap<>();
        for(var doc:docs)statuses.putIfAbsent(doc.getOrderNo(),doc.getStatus());
        var ids=rows.stream().map(org.dromara.insurance.domain.vo.InsuranceApplyRecordVo::getProductId).filter(java.util.Objects::nonNull).distinct().toList();
        java.util.Set<Long> enabled=new java.util.HashSet<>();
        if(!ids.isEmpty()) {
            var active=TenantHelper.dynamic("000000",()->products.selectList(new LambdaQueryWrapper<InsuranceProductConfig>()
                .in(InsuranceProductConfig::getId,ids).eq(InsuranceProductConfig::getApplicationFormRequired,true).select(InsuranceProductConfig::getId)));
            active.forEach(p->enabled.add(p.getId()));
        }
        for(var row:rows) {
            boolean required=Boolean.TRUE.equals(row.getApplicationFormRequired()) || (!Objects.equals(row.getStatus(),0)&&enabled.contains(row.getProductId()));
            row.setApplicationFormRequired(required);
            if(required)row.setApplicationFormStatus(statuses.getOrDefault(row.getOrderNo(),"MISSING"));
        }
    }
    public void enrichPlatform(java.util.List<org.dromara.insurance.domain.vo.InsuranceApplyRecordVo> rows) {
        if(rows.isEmpty())return;
        var tenantIds=rows.stream().map(org.dromara.insurance.domain.vo.InsuranceApplyRecordVo::getTenantId)
            .filter(Objects::nonNull).distinct().toList();
        var batchNos=rows.stream().filter(row->Objects.equals(row.getIsBatch(),1))
            .map(org.dromara.insurance.domain.vo.InsuranceApplyRecordVo::getOrderNo)
            .filter(Objects::nonNull).distinct().toList();
        java.util.List<InsuranceApplyRecord> children=java.util.List.of();
        if(!tenantIds.isEmpty()&&!batchNos.isEmpty()) {
            children=orders.selectList(new LambdaQueryWrapper<InsuranceApplyRecord>()
                .in(InsuranceApplyRecord::getTenantId,tenantIds)
                .in(InsuranceApplyRecord::getBatchOrderNo,batchNos)
                .eq(InsuranceApplyRecord::getIsBatch,2)
                .select(InsuranceApplyRecord::getId,InsuranceApplyRecord::getTenantId,
                    InsuranceApplyRecord::getOrderNo,InsuranceApplyRecord::getBatchOrderNo,
                    InsuranceApplyRecord::getProductId,InsuranceApplyRecord::getStatus,
                    InsuranceApplyRecord::getApplicationFormRequired));
        }
        java.util.Map<String,java.util.List<InsuranceApplyRecord>> childrenByBatch=new java.util.HashMap<>();
        for(var child:children)childrenByBatch.computeIfAbsent(platformKey(child.getTenantId(),child.getBatchOrderNo()),
            ignored->new java.util.ArrayList<>()).add(child);
        var nos=java.util.stream.Stream.concat(
                rows.stream().map(org.dromara.insurance.domain.vo.InsuranceApplyRecordVo::getOrderNo),
                children.stream().map(InsuranceApplyRecord::getOrderNo))
            .filter(Objects::nonNull).distinct().toList();
        java.util.Map<String,String> statuses=new java.util.HashMap<>();
        if(!tenantIds.isEmpty()&&!nos.isEmpty()) {
            var docs=documents.selectList(new LambdaQueryWrapper<InsuranceApplicationDocument>()
                .in(InsuranceApplicationDocument::getTenantId,tenantIds)
                .in(InsuranceApplicationDocument::getOrderNo,nos)
                .select(InsuranceApplicationDocument::getId,InsuranceApplicationDocument::getTenantId,
                    InsuranceApplicationDocument::getOrderNo,InsuranceApplicationDocument::getStatus)
                .orderByDesc(InsuranceApplicationDocument::getId));
            for(var doc:docs)statuses.putIfAbsent(platformKey(doc.getTenantId(),doc.getOrderNo()),doc.getStatus());
        }
        var ids=java.util.stream.Stream.concat(
                rows.stream().map(org.dromara.insurance.domain.vo.InsuranceApplyRecordVo::getProductId),
                children.stream().map(InsuranceApplyRecord::getProductId))
            .filter(Objects::nonNull).distinct().toList();
        java.util.Set<Long> enabled=new java.util.HashSet<>();
        if(!ids.isEmpty()) {
            var active=TenantHelper.dynamic("000000",()->products.selectList(new LambdaQueryWrapper<InsuranceProductConfig>()
                .in(InsuranceProductConfig::getId,ids).eq(InsuranceProductConfig::getApplicationFormRequired,true)
                .select(InsuranceProductConfig::getId)));
            active.forEach(p->enabled.add(p.getId()));
        }
        for(var row:rows) {
            if(Objects.equals(row.getIsBatch(),1)) {
                var batchChildren=childrenByBatch.getOrDefault(platformKey(row.getTenantId(),row.getOrderNo()),java.util.List.of());
                var requiredChildren=batchChildren.stream().filter(child->platformRequired(child.getApplicationFormRequired(),
                    child.getStatus(),child.getProductId(),enabled)).toList();
                boolean required=!requiredChildren.isEmpty();
                row.setApplicationFormRequired(required);
                if(required)row.setApplicationFormStatus(aggregateBatchStatus(requiredChildren,statuses));
                continue;
            }
            boolean required=Boolean.TRUE.equals(row.getApplicationFormRequired()) || (!Objects.equals(row.getStatus(),0)&&enabled.contains(row.getProductId()));
            row.setApplicationFormRequired(required);
            if(required)row.setApplicationFormStatus(statuses.getOrDefault(platformKey(row.getTenantId(),row.getOrderNo()),"MISSING"));
        }
    }
    private boolean platformRequired(Boolean snapshot,Integer status,Long productId,java.util.Set<Long> enabled){
        return Boolean.TRUE.equals(snapshot)||(!Objects.equals(status,0)&&enabled.contains(productId));
    }
    private String aggregateBatchStatus(java.util.List<InsuranceApplyRecord> children,java.util.Map<String,String> statuses){
        var childStatuses=children.stream().map(child->statuses.getOrDefault(
            platformKey(child.getTenantId(),child.getOrderNo()),"MISSING")).toList();
        if(childStatuses.stream().allMatch("READY"::equals))return "READY";
        for(String status:java.util.List.of("FAILED","INVALID","GENERATING","PARTIAL","DRAFT","MISSING"))
            if(childStatuses.contains(status))return status;
        return "MISSING";
    }
    private String platformKey(String tenantId,String orderNo){return tenantId+'\u0000'+orderNo;}
    public void assertDirectPersonEditAllowed(String orderNo) {
        if(orderNo==null)return;
        InsuranceApplyRecord order=orders.selectOne(new LambdaQueryWrapper<InsuranceApplyRecord>()
            .eq(InsuranceApplyRecord::getOrderNo,orderNo).last("FOR UPDATE"));
        if(order!=null && required(order))throw new ServiceException("签署订单的投被保人信息只能通过投保流程修改");
    }
    public InsuranceProductConfig product(Long id) {
        return TenantHelper.dynamic("000000", () -> products.selectById(id));
    }

    public boolean required(InsuranceApplyRecord order) {
        // 签字批次主单只汇总子单，不生成自己的投保单；子单通过快照字段继续锁定为需要签字。
        if (Objects.equals(order.getIsBatch(), 1)) return false;
        if (Boolean.TRUE.equals(order.getApplicationFormRequired())) return true;
        if (Objects.equals(order.getStatus(), 0)) return false;
        InsuranceProductConfig p = product(order.getProductId());
        return p != null && Boolean.TRUE.equals(p.getApplicationFormRequired());
    }

    public void assertBatchAllowed(Long productId) {
        InsuranceProductConfig p = product(productId);
        if (p != null && Boolean.TRUE.equals(p.getApplicationFormRequired()))
            throw new ServiceException("该产品需要本人签字，仅支持单人投保，不能批量投保");
    }

    public InsuranceApplyRecord lock(String orderNo) {
        InsuranceApplyRecord order = orders.selectOne(new LambdaQueryWrapper<InsuranceApplyRecord>()
            .eq(InsuranceApplyRecord::getOrderNo, orderNo).last("FOR UPDATE"));
        if (order == null) throw new ServiceException("订单不存在");
        return order;
    }

    public void assertOwner(InsuranceApplyRecord order) {
        if (!Objects.equals(order.getAgentUserId(), LoginHelper.getUserId()))
            throw new ServiceException("无权操作此订单");
    }

    public InsuranceApplicationDocument latest(String orderNo) {
        return documents.selectOne(new LambdaQueryWrapper<InsuranceApplicationDocument>()
            .eq(InsuranceApplicationDocument::getOrderNo, orderNo)
            .orderByDesc(InsuranceApplicationDocument::getId).last("LIMIT 1"));
    }

    public void beforeSave(InsuranceApplyRecord order, int insuredCount) {
        if (!required(order)) return;
        assertOwner(order);
        assertEditable(order);
        if (insuredCount != 1) throw new ServiceException("签字投保单每次仅支持一位被保险人");
        order.setApplicationFormRequired(true);
        invalidate(order.getOrderNo());
    }

    public void assertEditable(InsuranceApplyRecord order) {
        if (!java.util.Arrays.asList(1, 2, 3).contains(order.getStatus()))
            throw new ServiceException("已支付或已取消订单不能修改或重新签署");
    }
    public void invalidate(String orderNo) {
        documents.update(null, Wrappers.<InsuranceApplicationDocument>lambdaUpdate()
            .eq(InsuranceApplicationDocument::getOrderNo, orderNo)
            .ne(InsuranceApplicationDocument::getStatus, "INVALID")
            .set(InsuranceApplicationDocument::getStatus, "INVALID"));
    }

    public void assertReady(InsuranceApplyRecord order) {
        if (!required(order)) return;
        if (Objects.equals(order.getStatus(), 4)) throw new ServiceException("已取消订单不能支付");
        InsuranceApplicationDocument doc = latest(order.getOrderNo());
        if (doc == null || !"READY".equals(doc.getStatus()) || doc.getPdfKey() == null)
            throw new ServiceException("请先核对投保资料、完成三处签字并生成投保单后再支付");
        try {
            if (!sourceHash(order).equals(json.readTree(doc.getSnapshotJson()).path("sourceHash").asText()))
                throw new ServiceException("投保资料已变化，请重新核对并签字");
        } catch (java.io.IOException e) { throw new ServiceException("投保单资料无法校验"); }
        if (!Objects.equals(order.getStatus(), 0) && (order.getPolicyStartDate()==null ||
            !order.getPolicyStartDate().toInstant().atZone(java.time.ZoneId.of("Asia/Shanghai")).toLocalDate()
                .isAfter(java.time.LocalDate.now(java.time.ZoneId.of("Asia/Shanghai")))))
            throw new ServiceException("起保日期已过期，请修改资料后重新签署");
    }
}
