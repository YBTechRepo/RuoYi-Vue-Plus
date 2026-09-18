package org.dromara.insurance.service;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.DictService;
import org.dromara.insurance.domain.InsuranceApplicationDocument;
import org.dromara.insurance.domain.InsuranceApplyRecord;
import org.dromara.insurance.domain.InsuranceOrderApplicant;
import org.dromara.insurance.domain.InsuranceOrderInsured;
import org.dromara.insurance.domain.InsuranceProductConfig;
import org.dromara.insurance.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("dev")
class InsuranceApplicationFormServiceTest {
    private final ObjectMapper json = new ObjectMapper();
    private ApplicationFormGuard guard;
    private ApplicationFormStorage storage;
    private ApplicationFormTemplate template;
    private InsuranceApplicationDocumentMapper documents;
    private InsuranceApplyRecordMapper orders;
    private InsuranceOrderApplicantMapper applicants;
    private InsuranceOrderInsuredMapper insureds;
    private PlatformTransactionManager transactionManager;
    private DictService dictService;
    private InsuranceApplicationFormService service;
    private InsuranceApplicationDocument document;

    @BeforeEach
    void setup() throws Exception {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), "test"), InsuranceApplicationDocument.class);
        guard = mock(ApplicationFormGuard.class);
        storage = mock(ApplicationFormStorage.class);
        template = mock(ApplicationFormTemplate.class);
        documents = mock(InsuranceApplicationDocumentMapper.class);
        orders = mock(InsuranceApplyRecordMapper.class);
        applicants = mock(InsuranceOrderApplicantMapper.class);
        insureds = mock(InsuranceOrderInsuredMapper.class);
        dictService = mock(DictService.class);
        transactionManager = mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        service = new InsuranceApplicationFormService(guard, template, storage,
            orders, documents, applicants, insureds, transactionManager, dictService, json);
        var order = new InsuranceApplyRecord();
        order.setOrderNo("ORDER-001");
        when(guard.lock("ORDER-001")).thenReturn(order);
        when(guard.sourceHash(any())).thenReturn("source-hash");
        when(storage.config()).thenReturn("private-test");
        when(template.templateHash(anyString(), anyString())).thenReturn("template-hash");
        when(template.render(anyString(), anyString(), anyMap(), anyMap())).thenReturn("sample-pdf".getBytes());
        when(documents.update(isNull(), any())).thenReturn(1);
        document = new InsuranceApplicationDocument();
        document.setId(99L);
        document.setTenantId("tenant-test");
        document.setStatus("DRAFT");
        document.setSnapshotHash("snapshot-hash");
        document.setSnapshotJson("{\"sourceHash\":\"source-hash\"}");
        document.setTemplateCode(ApplicationFormTemplate.CODE);
        document.setTemplateVersion(ApplicationFormTemplate.VERSION);
        document.setTemplateHash("template-hash");
        when(guard.latest("ORDER-001")).thenReturn(document);
    }

    private Map<String, byte[]> images() throws Exception {
        byte[] image = ApplicationFormTemplateTest.signature(false);
        return Map.of("special", image, "applicant", image, "insured", image);
    }

    @Test
    void statusExposesDocumentTemplateForMatchingMobileStatements() {
        var order = new InsuranceApplyRecord();
        order.setOrderNo("ORDER-001");
        var result = service.status(order);
        assertEquals(ApplicationFormTemplate.CODE, result.get("templateCode"));
        assertEquals(ApplicationFormTemplate.VERSION, result.get("templateVersion"));
    }

    @Test
    void signsThreeSeparateSlotsBeforeArchivingPdf() throws Exception {
        var result = service.sign("ORDER-001", 99L, "snapshot-hash", images(), true, "127.0.0.1", "test");
        assertEquals("READY", result.get("status"));
        assertNotNull(document.getSignedTime());
        assertEquals("private-test", document.getStorageConfig());
        verify(storage, times(3)).put(eq("private-test"), endsWith(".png"), any(), eq("image/png"));
        verify(storage).put(eq("private-test"), endsWith("application.pdf"), any(), eq("application/pdf"));
        verify(template).render(eq(ApplicationFormTemplate.CODE), eq(ApplicationFormTemplate.VERSION),
            anyMap(), argThat(signatures -> signatures.size() == 3));
    }

    @Test
    void refusesUncheckedStatementsAndStaleSnapshotsBeforeUploading() throws Exception {
        var images = images();
        assertThrows(ServiceException.class, () -> service.sign("ORDER-001", 99L, "snapshot-hash", images, false, "", ""));
        assertThrows(ServiceException.class, () -> service.sign("ORDER-001", 99L, "old-hash", images, true, "", ""));
        when(guard.sourceHash(any())).thenReturn("changed-source");
        assertThrows(ServiceException.class, () -> service.sign("ORDER-001", 99L, "snapshot-hash", images, true, "", ""));
        verify(storage, never()).put(anyString(), anyString(), any(), anyString());
        verify(documents, never()).updateById(any(InsuranceApplicationDocument.class));
    }

    @Test
    void failedStorageRetainsSignedPayloadAndRetryCanFinish() throws Exception {
        doThrow(new IllegalStateException("storage unavailable")).doNothing()
            .when(storage).put(anyString(), anyString(), any(), anyString());
        assertThrows(ServiceException.class, () -> service.sign("ORDER-001", 99L, "snapshot-hash", images(), true, "", ""));
        assertNotNull(document.getSignatureJson());
        assertEquals(3, json.readTree(document.getSignatureJson()).size());
        // Simulate the persisted failure update before a later request reads the row.
        document.setStatus("FAILED");
        assertEquals("READY", service.generate("ORDER-001", 99L).get("status"));
    }

    @Test
    void concurrentInvalidationCannotPublishReadyDocument() throws Exception {
        when(documents.update(isNull(), any())).thenReturn(0);
        assertThrows(ServiceException.class, () -> service.sign("ORDER-001", 99L, "snapshot-hash", images(), true, "", ""));
        verify(documents, times(2)).update(isNull(), any());
    }

    @Test
    void rejectsReplacedVersionAndActiveGeneration() throws Exception {
        assertThrows(ServiceException.class, () -> service.generate("ORDER-001", 98L));
        document.setStatus("GENERATING");
        document.setUpdateTime(new Date());
        document.setSignatureJson(json.writeValueAsString(Map.of("special", Base64.getEncoder().encodeToString(new byte[]{1}))));
        assertThrows(ServiceException.class, () -> service.generate("ORDER-001", 99L));
        verifyNoInteractions(storage);
    }

    @Test
    void archivedPdfMustMatchItsSavedDigest() {
        document.setStatus("READY");
        document.setStorageConfig("private-test");
        document.setPdfKey("insurance-applications/file.pdf");
        document.setPdfHash("expected-hash");
        when(storage.get(anyString(), anyString())).thenReturn("tampered".getBytes());
        assertThrows(ServiceException.class, () -> service.pdf("ORDER-001"));
    }

    @Test
    void platformArchiveReturnsThePdfSnapshotAndSavedPeopleFromTheSameTenant() {
        byte[] pdf = "archived-pdf".getBytes();
        var order = new InsuranceApplyRecord();
        order.setId(1L);
        order.setTenantId("tenant-test");
        order.setOrderNo("ORDER-001");
        order.setInsureMode(1);
        when(orders.selectOne(any())).thenReturn(order);
        when(guard.required(order)).thenReturn(true);

        document.setStatus("READY");
        document.setStorageConfig("private-test");
        document.setPdfKey("insurance-applications/file.pdf");
        document.setPdfHash(ApplicationFormTemplate.hash(pdf));
        document.setSnapshotJson("{\"applicantName\":\"张三\",\"insuredName\":\"李四\"}");
        when(documents.selectOne(any())).thenReturn(document);
        when(storage.get("private-test", "insurance-applications/file.pdf")).thenReturn(pdf);
        var applicant = new InsuranceOrderApplicant();
        applicant.setApplicantName("张三");
        var insured = new InsuranceOrderInsured();
        insured.setInsuredName("李四");
        when(applicants.selectOne(any())).thenReturn(applicant);
        when(insureds.selectOne(any())).thenReturn(insured);

        var archive = service.platformArchive(1L);

        assertArrayEquals(pdf, archive.bytes());
        assertEquals("张三", archive.snapshot().get("applicantName"));
        assertEquals("张三", archive.applicant().getApplicantName());
        assertEquals("李四", archive.insured().getInsuredName());
    }

    @Test
    void derivesMinorIdentityAndSignerFromSavedPeopleAndIgnoresLegacyInputs() throws Exception {
        Map<String,Object> snapshot = snapshot("0", "110101201601010015", "1999-12-31", "女");
        assertEquals("2016-01-01", snapshot.get("insuredBirthday"));
        assertEquals("男", snapshot.get("insuredGender"));
        assertEquals("法定监护人", snapshot.get("insuredSignerRole"));
        assertEquals("测试投保人", snapshot.get("insuredSignerName"));
        assertEquals("法定", snapshot.get("beneficiary"));
        assertEquals("新投保业务", snapshot.get("businessType"));
        assertEquals("诉讼", snapshot.get("disputeMode"));
        assertFalse(snapshot.containsKey("previousPolicyNo"));
        assertFalse(snapshot.containsKey("otherPolicyAnswer"));
        assertFalse(snapshot.containsKey("otherPoliciesSummary"));
        assertFalse(snapshot.containsKey("arbitrationCommittee"));
    }

    @Test
    void keepsRequiredIdentityInputForNonIdCardAndUsesAdultInsuredSigner() throws Exception {
        Map<String,Object> snapshot = snapshot("1", "P1234567", "1990-01-01", "女");
        assertEquals("1990-01-01", snapshot.get("insuredBirthday"));
        assertEquals("女", snapshot.get("insuredGender"));
        assertEquals("被保险人", snapshot.get("insuredSignerRole"));
        assertEquals("测试被保人", snapshot.get("insuredSignerName"));
    }

    @SuppressWarnings("unchecked")
    private Map<String,Object> snapshot(String certType,String certNo,String birthday,String gender) throws Exception {
        var realTemplate = new ApplicationFormTemplate();
        var snapshotService = new InsuranceApplicationFormService(guard, realTemplate, storage,
            orders, documents, applicants, insureds, transactionManager, dictService, json);
        var order = new InsuranceApplyRecord();
        order.setOrderNo("SNAPSHOT-001");order.setProductId(1L);order.setPaymentMode(1);
        order.setPremium(new BigDecimal("50.00"));
        ZoneId zone=ZoneId.of("Asia/Shanghai");
        order.setPolicyStartDate(Date.from(LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant()));
        Map<String,Object> raw=new LinkedHashMap<>(ApplicationFormTemplateTest.sample());
        raw.put("insuredBirthday",birthday);raw.put("insuredGender",gender);
        raw.put("businessType","续保业务");raw.put("previousPolicyNo","OLD-POLICY-001");
        raw.put("beneficiary","指定受益人");
        raw.put("disputeMode","仲裁");raw.put("arbitrationCommittee","旧仲裁委员会");
        raw.put("otherPolicyAnswer","有");raw.put("otherPoliciesSummary","旧保单资料");
        order.setInsureExtraData(json.writeValueAsString(Map.of("applicationForm",raw)));
        var applicant=new InsuranceOrderApplicant();applicant.setOrderNo(order.getOrderNo());
        applicant.setApplicantName("测试投保人");applicant.setApplicantPhone("13800000000");
        applicant.setApplicantAddress("测试地址");applicant.setApplicantCertNo("110101199001010010");applicant.setApplicantCertType("0");
        var insured=new InsuranceOrderInsured();insured.setOrderNo(order.getOrderNo());insured.setRelation("3");
        insured.setInsuredName("测试被保人");insured.setInsuredPhone("13800000001");
        insured.setInsuredAddress("测试地址");insured.setInsuredCertType(certType);insured.setInsuredCertNo(certNo);
        when(applicants.selectOne(any())).thenReturn(applicant);
        when(insureds.selectList(any())).thenReturn(List.of(insured));
        when(dictService.getDictLabel("insurance_id_type","0")).thenReturn("身份证");
        when(dictService.getDictLabel("insurance_id_type",certType)).thenReturn("0".equals(certType)?"身份证":"护照");
        when(dictService.getDictLabel("insurance_relationship_to_insured","3")).thenReturn("子女");
        var product=new InsuranceProductConfig();product.setMinPremium(new BigDecimal("50.00"));
        product.setApplicationTemplateCode(ApplicationFormTemplate.CODE);
        product.setApplicationTemplateVersion(ApplicationFormTemplate.VERSION);
        return ReflectionTestUtils.invokeMethod(snapshotService,"snapshot",order,product);
    }
}
