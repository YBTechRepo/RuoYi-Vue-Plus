package org.dromara.insurance.service;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.DictService;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.insurance.domain.*;
import org.dromara.insurance.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import static org.dromara.insurance.service.ApplicationFormTemplate.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsuranceApplicationFormService {
    private final ApplicationFormGuard guard;
    private final ApplicationFormTemplate template;
    private final ApplicationFormStorage storage;
    private final InsuranceApplyRecordMapper orders;
    private final InsuranceApplicationDocumentMapper documents;
    private final InsuranceOrderApplicantMapper applicants;
    private final InsuranceOrderInsuredMapper insureds;
    private final PlatformTransactionManager transactionManager;
    private final DictService dictService;
    private final ObjectMapper json;
    private <T>T tx(Supplier<T> action){return new TransactionTemplate(transactionManager).execute(status->action.get());}
    private String encode(Object value){try{return json.writeValueAsString(value);}catch(Exception e){throw new ServiceException("投保单资料格式错误");}}
    private Map<String,Object> decode(String value){try{return json.readValue(value,new TypeReference<LinkedHashMap<String,Object>>(){});}catch(Exception e){throw new ServiceException("投保单资料格式错误");}}
    public JsonNode metadata(){return template.metadata();}
    public JsonNode metadata(String code,String version){return template.metadata(code,version);}

    @DataPermission({@DataColumn(key="deptName",value="agent_dept_id"),@DataColumn(key="userName",value="agent_user_id")})
    public InsuranceApplyRecord readable(String orderNo){
        InsuranceApplyRecord order=orders.selectOne(new LambdaQueryWrapper<InsuranceApplyRecord>().eq(InsuranceApplyRecord::getOrderNo,orderNo));
        if(order==null)throw new ServiceException("订单不存在或无权访问");return order;
    }
    // Controller calls readable through the Spring proxy before every read/export; mutations also require exact ownership.
    public Map<String,Object> status(InsuranceApplyRecord order){
        return status(order,guard.latest(order.getOrderNo()));
    }
    private Map<String,Object> status(InsuranceApplyRecord order,InsuranceApplicationDocument doc){
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("required",guard.required(order));
        result.put("status",doc==null?"MISSING":doc.getStatus());
        if(doc!=null){result.put("documentId",doc.getId().toString());result.put("snapshotHash",doc.getSnapshotHash());
            result.put("templateCode",doc.getTemplateCode());result.put("templateVersion",doc.getTemplateVersion());
            result.put("signedTime",doc.getSignedTime());result.put("failureReason",doc.getFailureReason());
            var values=decode(doc.getSnapshotJson());result.put("applicantName",values.get("applicantName"));
            result.put("insuredSignerName",values.get("insuredSignerName"));result.put("insuredSignerRole",values.get("insuredSignerRole"));}
        return result;
    }
    public Map<String,Object> platformStatus(Long orderId){
        return TenantHelper.ignore(()->{
            InsuranceApplyRecord order=platformOrder(orderId);
            return status(order,platformLatest(order));
        });
    }
    public PlatformPdf platformPdf(Long orderId){
        return TenantHelper.ignore(()->{
            InsuranceApplyRecord order=platformOrder(orderId);
            if(!guard.required(order))throw new ServiceException("该订单未启用签字投保单");
            return new PlatformPdf(order.getOrderNo(),readPdf(platformLatest(order)));
        });
    }
    public PlatformArchive platformArchive(Long orderId){
        return TenantHelper.ignore(()->{
            InsuranceApplyRecord order=platformOrder(orderId);
            if(!guard.required(order))throw new ServiceException("该订单未启用签字投保单");
            InsuranceApplicationDocument doc=platformLatest(order);
            byte[] pdf=readPdf(doc);
            InsuranceOrderApplicant applicant=applicants.selectOne(new LambdaQueryWrapper<InsuranceOrderApplicant>()
                .eq(InsuranceOrderApplicant::getTenantId,order.getTenantId())
                .eq(InsuranceOrderApplicant::getOrderNo,order.getOrderNo()));
            InsuranceOrderInsured insured=insureds.selectOne(new LambdaQueryWrapper<InsuranceOrderInsured>()
                .eq(InsuranceOrderInsured::getTenantId,order.getTenantId())
                .eq(InsuranceOrderInsured::getOrderNo,order.getOrderNo())
                .orderByAsc(InsuranceOrderInsured::getId).last("LIMIT 1"));
            return new PlatformArchive(order.getOrderNo(),doc.getTemplateCode(),doc.getTemplateVersion(),
                decode(doc.getSnapshotJson()),applicant,insured,pdf);
        });
    }
    private InsuranceApplyRecord platformOrder(Long orderId){
        InsuranceApplyRecord order=orders.selectOne(new LambdaQueryWrapper<InsuranceApplyRecord>()
            .eq(InsuranceApplyRecord::getId,orderId).eq(InsuranceApplyRecord::getInsureMode,1));
        if(order==null)throw new ServiceException("代投保订单不存在");
        return order;
    }
    private InsuranceApplicationDocument platformLatest(InsuranceApplyRecord order){
        return documents.selectOne(new LambdaQueryWrapper<InsuranceApplicationDocument>()
            .eq(InsuranceApplicationDocument::getTenantId,order.getTenantId())
            .eq(InsuranceApplicationDocument::getOrderNo,order.getOrderNo())
            .orderByDesc(InsuranceApplicationDocument::getId).last("LIMIT 1"));
    }
    public record PlatformPdf(String orderNo,byte[] bytes){}
    public record PlatformArchive(String orderNo,String templateCode,String templateVersion,
                                  Map<String,Object> snapshot,InsuranceOrderApplicant applicant,
                                  InsuranceOrderInsured insured,byte[] bytes){}
    public Map<String,Object> prepare(String orderNo){
        InsuranceApplicationDocument doc=tx(()->{
            InsuranceApplyRecord order=guard.lock(orderNo);guard.assertOwner(order);
            guard.assertEditable(order);
            if(!guard.required(order))throw new ServiceException("该订单未启用签字投保单");
            InsuranceApplicationDocument old=guard.latest(orderNo);
            if(old!=null&&!"INVALID".equals(old.getStatus()))return old;
            InsuranceProductConfig product=guard.product(order.getProductId());
            if(product==null)throw new ServiceException("产品不存在");
            // 历史订单已锁定签署要求，即使产品后来关闭开关，仍需校验其保留配置。
            product.setApplicationFormRequired(true);
            template.validateProduct(product);
            String templateCode=product.getApplicationTemplateCode();
            String templateVersion=product.getApplicationTemplateVersion();
            Map<String,Object> snapshot=snapshot(order,product);
            try{template.render(templateCode,templateVersion,snapshot,Map.of());}catch(IOException e){throw new ServiceException("投保单预览生成失败");}
            var created=new InsuranceApplicationDocument();created.setOrderNo(orderNo);
            created.setTemplateCode(templateCode);created.setTemplateVersion(templateVersion);
            created.setTemplateHash(template.templateHash(templateCode,templateVersion));created.setSnapshotJson(encode(snapshot));
            created.setSnapshotHash(hash(created.getSnapshotJson().getBytes(StandardCharsets.UTF_8)));created.setStatus("DRAFT");
            documents.insert(created);
            orders.update(null,Wrappers.<InsuranceApplyRecord>lambdaUpdate().eq(InsuranceApplyRecord::getId,order.getId()).set(InsuranceApplyRecord::getApplicationFormRequired,true));
            return created;
        });
        return Map.of("documentId",doc.getId().toString(),"snapshotHash",doc.getSnapshotHash(),"status",doc.getStatus());
    }
    public byte[] preview(String orderNo,Long documentId){
        InsuranceApplicationDocument doc=current(orderNo,documentId);
        try{return template.render(doc.getTemplateCode(),doc.getTemplateVersion(),decode(doc.getSnapshotJson()),Map.of());}
        catch(IOException e){throw new ServiceException("投保单预览生成失败");}
    }
    public int pageCount(String orderNo, Long id) throws IOException {
        try (var pdf=org.apache.pdfbox.pdmodel.PDDocument.load(viewBytes(orderNo,id))) { return pdf.getNumberOfPages(); }
    }
    private byte[] viewBytes(String orderNo,Long id) {
        var doc=current(orderNo,id);
        return "READY".equals(doc.getStatus())?pdf(orderNo):preview(orderNo,id);
    }
    public byte[] page(String orderNo,Long id,int page) throws IOException {
        try(var pdf=org.apache.pdfbox.pdmodel.PDDocument.load(viewBytes(orderNo,id));var out=new ByteArrayOutputStream()) {
            if(page<1||page>pdf.getNumberOfPages())throw new ServiceException("页码超出范围");
            var image=new org.apache.pdfbox.rendering.PDFRenderer(pdf).renderImageWithDPI(page-1,120);
            javax.imageio.ImageIO.write(image,"png",out);return out.toByteArray();
        }
    }
    public Map<String,Object> input(InsuranceApplyRecord order) {
        guard.assertOwner(order);
        var applicant=applicants.selectOne(new LambdaQueryWrapper<InsuranceOrderApplicant>().eq(InsuranceOrderApplicant::getOrderNo,order.getOrderNo()));
        var list=insureds.selectList(new LambdaQueryWrapper<InsuranceOrderInsured>().eq(InsuranceOrderInsured::getOrderNo,order.getOrderNo()));
        var result=new LinkedHashMap<String,Object>();result.put("applicant",applicant);result.put("insuredList",list);return result;
    }
    private InsuranceApplicationDocument current(String orderNo,Long id){
        InsuranceApplicationDocument doc=guard.latest(orderNo);
        if(doc==null||!Objects.equals(doc.getId(),id)||"INVALID".equals(doc.getStatus()))throw new ServiceException("资料版本已变化，请重新核对投保单并签字");
        return doc;
    }
    public Map<String,Object> sign(String orderNo,Long id,String snapshotHash,Map<String,byte[]> images,boolean confirmed,String ip,String ua)throws IOException{
        if(!confirmed)throw new ServiceException("请逐一确认三处声明后签署");
        Map<String,Object> payload=new LinkedHashMap<>();
        for(String key:List.of("special","applicant","insured")){
            byte[] bytes=images.get(key);if(bytes==null)throw new ServiceException("请完成三处签字");
            payload.put(key,Base64.getEncoder().encodeToString(normalizeSignature(bytes)));
        }
        String storageConfig=storage.config();
        InsuranceApplicationDocument doc=tx(()->{
            var order=guard.lock(orderNo);guard.assertOwner(order);
            guard.assertEditable(order);
            var current=current(orderNo,id);
            if(!Objects.equals(text(decode(current.getSnapshotJson()),"sourceHash"),guard.sourceHash(order)))
                throw new ServiceException("投保资料已变化，请重新核对并签字");
            if(!Objects.equals(current.getSnapshotHash(),snapshotHash))throw new ServiceException("投保单版本不一致，请重新核对");
            if(!"DRAFT".equals(current.getStatus()))return current;
            current.setSignatureJson(encode(payload));current.setSignedTime(new Date());current.setRequestIp(ip);
            current.setUserAgent(ua==null?"":ua.substring(0,Math.min(ua.length(),500)));current.setStorageConfig(storageConfig);
            current.setStatus("FAILED");current.setFailureReason("等待生成");documents.updateById(current);return current;
        });
        return generate(orderNo,doc.getId());
    }
    public Map<String,Object> signPublic(String orderNo,Long id,List<String> slots,Map<String,byte[]> images,String ip,String ua)throws IOException{
        if(slots==null||slots.isEmpty())throw new ServiceException("签署栏位不能为空");
        Map<String,Object> incoming=new LinkedHashMap<>();
        for(String key:slots){
            if(!List.of("special","applicant","insured").contains(key))throw new ServiceException("签署栏位无效");
            byte[] bytes=images.get(key);if(bytes==null)throw new ServiceException("请完成本页全部签字");
            incoming.put(key,Base64.getEncoder().encodeToString(normalizeSignature(bytes)));
        }
        String storageConfig=storage.config();
        InsuranceApplicationDocument doc=tx(()->{
            var order=guard.lock(orderNo);guard.assertEditable(order);
            var current=current(orderNo,id);
            if(!Objects.equals(text(decode(current.getSnapshotJson()),"sourceHash"),guard.sourceHash(order)))
                throw new ServiceException("投保资料已变化，请联系经办人重新发起签署");
            if(!List.of("DRAFT","PARTIAL").contains(current.getStatus())) {
                if("READY".equals(current.getStatus()))return current;
                throw new ServiceException("投保单当前状态不能签署");
            }
            Map<String,Object> payload=current.getSignatureJson()==null?new LinkedHashMap<>():decode(current.getSignatureJson());
            payload.putAll(incoming);current.setSignatureJson(encode(payload));current.setRequestIp(ip);
            current.setUserAgent(ua==null?"":ua.substring(0,Math.min(ua.length(),500)));current.setStorageConfig(storageConfig);
            boolean complete=List.of("special","applicant","insured").stream().allMatch(payload::containsKey);
            current.setStatus(complete?"FAILED":"PARTIAL");
            current.setFailureReason(complete?"等待生成":null);
            if(complete)current.setSignedTime(new Date());
            documents.updateById(current);return current;
        });
        if("READY".equals(doc.getStatus()))return Map.of("status","READY","documentId",id.toString());
        if("PARTIAL".equals(doc.getStatus()))return Map.of("status","PARTIAL","documentId",id.toString());
        return generateInternal(orderNo,doc.getId(),false);
    }
    public Map<String,Object> generate(String orderNo,Long id){
        return generateInternal(orderNo,id,true);
    }
    private Map<String,Object> generateInternal(String orderNo,Long id,boolean requireOwner){
        InsuranceApplicationDocument doc=tx(()->{
            var order=guard.lock(orderNo);if(requireOwner)guard.assertOwner(order);
            var current=current(orderNo,id);
            if("READY".equals(current.getStatus()))return current;
            guard.assertEditable(order);
            if("GENERATING".equals(current.getStatus())&&current.getUpdateTime()!=null
                &&current.getUpdateTime().getTime()>System.currentTimeMillis()-180000)throw new ServiceException("正在生成投保单，请稍后刷新");
            if(current.getSignatureJson()==null)throw new ServiceException("请先完成签字");
            current.setStatus("GENERATING");current.setGenerationToken(UUID.randomUUID().toString());
            documents.updateById(current);return current;
        });
        if("READY".equals(doc.getStatus()))return Map.of("status","READY","documentId",id.toString());
        long start=System.currentTimeMillis();
        try{
            if(!template.templateHash(doc.getTemplateCode(),doc.getTemplateVersion()).equals(doc.getTemplateHash()))
                throw new ServiceException("模板版本摘要不一致");
            Map<String,Object> payload=decode(doc.getSignatureJson());Map<String,byte[]> signatures=new LinkedHashMap<>();
            Map<String,Object> refs=new LinkedHashMap<>();
            String prefix="insurance-applications/"+doc.getTenantId()+"/"+id+"/"+doc.getGenerationToken()+"/";
            for(String key:List.of("special","applicant","insured")){
                byte[] bytes=Base64.getDecoder().decode(payload.get(key).toString());signatures.put(key,bytes);
                String objectKey=prefix+key+".png";storage.put(doc.getStorageConfig(),objectKey,bytes,"image/png");
                refs.put(key,Map.of("key",objectKey,"hash",hash(bytes),"confirmed",true));
            }
            Map<String,Object> values=decode(doc.getSnapshotJson());
            values.put("signatureDate",doc.getSignedTime().toInstant().atZone(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
            byte[] pdf=template.render(doc.getTemplateCode(),doc.getTemplateVersion(),values,signatures);
            String key=prefix+"application.pdf";
            storage.put(doc.getStorageConfig(),key,pdf,"application/pdf");
            Boolean completed=tx(()->{
                guard.lock(orderNo);
                return documents.update(null,Wrappers.<InsuranceApplicationDocument>lambdaUpdate()
                    .eq(InsuranceApplicationDocument::getId,id).eq(InsuranceApplicationDocument::getStatus,"GENERATING")
                    .eq(InsuranceApplicationDocument::getGenerationToken,doc.getGenerationToken())
                    .set(InsuranceApplicationDocument::getStatus,"READY").set(InsuranceApplicationDocument::getPdfKey,key)
                    .set(InsuranceApplicationDocument::getPdfHash,hash(pdf)).set(InsuranceApplicationDocument::getSignatureJson,encode(refs))
                    .set(InsuranceApplicationDocument::getFailureReason,null))==1;
            });
            if(!Boolean.TRUE.equals(completed))throw new ServiceException("资料已变更，本次文件已失效，请重新签署");
            log.info("投保单生成完成 documentId={} elapsedMs={}",id,System.currentTimeMillis()-start);
            return Map.of("status","READY","documentId",id.toString());
        }catch(Exception e){
            documents.update(null,Wrappers.<InsuranceApplicationDocument>lambdaUpdate().eq(InsuranceApplicationDocument::getId,id)
                .eq(InsuranceApplicationDocument::getStatus,"GENERATING").eq(InsuranceApplicationDocument::getGenerationToken,doc.getGenerationToken())
                .set(InsuranceApplicationDocument::getStatus,"FAILED").set(InsuranceApplicationDocument::getFailureReason,"文件生成或存储失败，请重试"));
            log.error("投保单生成失败 documentId={} errorType={}",id,e.getClass().getSimpleName());
            throw new ServiceException("投保单生成失败，尚未扣款，请重试");
        }
    }
    public byte[] pdf(String orderNo){
        return readPdf(guard.latest(orderNo));
    }
    private byte[] readPdf(InsuranceApplicationDocument doc){
        if(doc==null||!"READY".equals(doc.getStatus()))throw new ServiceException("该订单暂无已归档投保单");
        byte[] bytes=storage.get(doc.getStorageConfig(),doc.getPdfKey());
        if(!hash(bytes).equals(doc.getPdfHash()))throw new ServiceException("投保单文件校验失败");return bytes;
    }
    private Map<String,Object> snapshot(InsuranceApplyRecord order,InsuranceProductConfig product){
        var a=applicants.selectOne(new LambdaQueryWrapper<InsuranceOrderApplicant>().eq(InsuranceOrderApplicant::getOrderNo,order.getOrderNo()));
        var people=insureds.selectList(new LambdaQueryWrapper<InsuranceOrderInsured>().eq(InsuranceOrderInsured::getOrderNo,order.getOrderNo()));
        if(a==null||people.size()!=1)throw new ServiceException("请先保存完整的单人投保资料");
        var i=people.getFirst();
        Map<String,Object> extras=decode(Objects.toString(order.getInsureExtraData(),"{}"));
        Object raw=extras.get("applicationForm");
        if(!(raw instanceof Map<?,?>))throw new ServiceException("请补充投保单资料");
        Map<String,Object> v=new LinkedHashMap<>();
        for(JsonNode field:template.metadata().path("fields")) {
            String key=field.path("key").asText();v.put(key,((Map<?,?>)raw).get(key));
        }
        v.put("orderNo",order.getOrderNo());v.put("applicantName",a.getApplicantName());v.put("applicantPhone",a.getApplicantPhone());
        v.put("applicantAddress",a.getApplicantAddress());v.put("applicantCertNo",a.getApplicantCertNo());
        v.put("insuredName",i.getInsuredName());v.put("insuredPhone",i.getInsuredPhone());v.put("insuredAddress",i.getInsuredAddress());v.put("insuredCertNo",i.getInsuredCertNo());
        v.put("applicantCertTypeLabel",cert(a.getApplicantCertType()));v.put("insuredCertTypeLabel",cert(i.getInsuredCertType()));
        String relation=dictService.getDictLabel("insurance_relationship_to_insured",i.getRelation());
        v.put("relationLabel",Arrays.asList("本人","配偶","父母","子女").contains(relation)?relation:"其他");
        LocalDate today=LocalDate.now(ZoneId.of("Asia/Shanghai"));
        LocalDate birthday;
        if("0".equals(i.getInsuredCertType())&&i.getInsuredCertNo()!=null&&i.getInsuredCertNo().matches("[0-9]{17}[0-9Xx]")){
            try{birthday=LocalDate.parse(i.getInsuredCertNo().substring(6,14),DateTimeFormatter.BASIC_ISO_DATE);}
            catch(Exception e){throw new ServiceException("被保险人身份证出生日期无效");}
            v.put("insuredBirthday",birthday.toString());
            v.put("insuredGender",(i.getInsuredCertNo().charAt(16)-'0')%2==1?"男":"女");
        }else{
            try{birthday=LocalDate.parse(text(v,"insuredBirthday"));}catch(Exception e){throw new ServiceException("请填写正确的被保险人出生日期");}
        }
        if(birthday.isAfter(today))throw new ServiceException("出生日期不能晚于今天");
        boolean minor=birthday.plusYears(18).isAfter(today);
        v.put("insuredSignerRole",minor?"法定监护人":"被保险人");
        v.put("insuredSignerName",minor?a.getApplicantName():i.getInsuredName());
        v.put("beneficiary","法定");
        v.put("businessType","新投保业务");
        v.put("disputeMode","诉讼");
        template.validateFields(v);
        if(order.getPolicyStartDate()==null)throw new ServiceException("请填写起保日期");
        LocalDate start=order.getPolicyStartDate().toInstant().atZone(ZoneId.of("Asia/Shanghai")).toLocalDate();
        if(!start.isAfter(LocalDate.now(ZoneId.of("Asia/Shanghai"))))throw new ServiceException("起保日期已过期，请修改资料后重新签署");
        int months=template.insuranceMonths(product.getApplicationTemplateCode(),product.getApplicationTemplateVersion());
        var format=DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        v.put("insurancePeriod","自"+start.format(format)+"零时起至"+start.plusMonths(months).minusDays(1).format(format)+"二十四时止");
        BigDecimal premium=order.getPremium();
        BigDecimal templatePremium=template.premium(product.getApplicationTemplateCode(),product.getApplicationTemplateVersion());
        if(premium==null||premium.compareTo(product.getMinPremium())!=0||premium.compareTo(templatePremium)!=0)
            throw new ServiceException("产品保费或投保单模板已变化，请重新创建订单");
        v.put("premium",premium.toPlainString());v.put("premiumUpper",Convert.digitToChinese(premium.doubleValue()));
        v.put("paymentMethod",Objects.equals(order.getPaymentMode(),1)?"余额支付":"常规支付");
        v.put("sourceHash",guard.sourceHash(order));return v;
    }
    private String cert(String code){String label=dictService.getDictLabel("insurance_id_type",code);return Arrays.asList("身份证","护照").contains(label)?label:"其他";}
}
