package org.dromara.insurance.service;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.insurance.domain.InsuranceApplicationSignInvite;
import org.dromara.insurance.domain.InsuranceApplyRecord;
import org.dromara.insurance.domain.dto.BatchSubmitDTO;
import org.dromara.insurance.domain.dto.SignedBatchInsuredImportDto;
import org.dromara.insurance.mapper.InsuranceApplicationDocumentMapper;
import org.dromara.insurance.mapper.InsuranceApplyRecordMapper;
import org.dromara.insurance.mapper.InsuranceOrderApplicantMapper;
import org.dromara.insurance.mapper.InsuranceOrderInsuredMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("dev")
class SignedBatchInsuranceServiceTest {
    private SignedBatchTemplateDescriptor templates;
    private ApplicationFormTemplate applicationTemplate;
    private InsuranceApplyRecordMapper orders;
    private InsurancePublicSignService publicSignService;
    private SignedBatchInsuranceService service;

    @BeforeEach
    void setup() {
        templates = mock(SignedBatchTemplateDescriptor.class);
        applicationTemplate = mock(ApplicationFormTemplate.class);
        orders = mock(InsuranceApplyRecordMapper.class);
        publicSignService = mock(InsurancePublicSignService.class);
        service = new SignedBatchInsuranceService(templates, applicationTemplate,
            mock(ApplicationFormGuard.class), mock(InsuranceApplicationFormService.class),
            mock(IInsuranceProductConfigService.class), mock(IInsuranceApplyRecordService.class),
            orders, mock(InsuranceOrderApplicantMapper.class),
            mock(InsuranceOrderInsuredMapper.class), mock(InsuranceApplicationDocumentMapper.class),
            mock(org.dromara.finance.service.IBizUserAccountService.class), publicSignService);
    }

    @Test
    void selfRelationCopiesApplicantAndDerivesIdCardFields() {
        SignedBatchTemplateDescriptor.Descriptor descriptor = descriptor("schema-hash");
        when(templates.describe(1L)).thenReturn(descriptor);
        BatchSubmitDTO submit = submission();
        SignedBatchInsuredImportDto row = applicantOnlyRow();
        submit.setSignedAuditList(List.of(row));

        assertDoesNotThrow(() -> service.validateSubmission(submit));
        assertEquals(row.getAppName(), row.getName());
        assertEquals(row.getAppCertNo(), row.getCertNo());
        assertEquals("1949-12-31", row.getApplicationForm().get("insuredBirthday"));
        assertEquals("女", row.getApplicationForm().get("insuredGender"));
        verify(applicationTemplate).validateFields(row.getApplicationForm());
    }

    @Test
    void rejectsClientModeOrStaleTemplateMetadata() {
        SignedBatchTemplateDescriptor.Descriptor descriptor = descriptor("current-hash");
        when(templates.describe(1L)).thenReturn(descriptor);
        BatchSubmitDTO wrongMode = submission();
        wrongMode.setTemplateMode("STANDARD");
        wrongMode.setSignedAuditList(List.of(applicantOnlyRow()));
        assertThrows(ServiceException.class, () -> service.validateSubmission(wrongMode));

        BatchSubmitDTO stale = submission();
        stale.setSchemaHash("old-hash");
        stale.setSignedAuditList(List.of(applicantOnlyRow()));
        assertThrows(ServiceException.class, () -> service.validateSubmission(stale));
    }

    @Test
    void signedBatchIsLimitedToFiftyPeople() {
        SignedBatchTemplateDescriptor.Descriptor descriptor = descriptor("schema-hash");
        when(templates.describe(1L)).thenReturn(descriptor);
        BatchSubmitDTO submit = submission();
        List<SignedBatchInsuredImportDto> rows = new ArrayList<>();
        for (int i = 0; i < 51; i++) rows.add(applicantOnlyRow());
        submit.setSignedAuditList(rows);
        assertThrows(ServiceException.class, () -> service.validateSubmission(submit));
        verifyNoInteractions(applicationTemplate);
    }

    @Test
    void issueAllLinksCopiesOnlyPendingInvites() {
        InsuranceApplyRecord main = new InsuranceApplyRecord();
        main.setOrderNo("BH001");
        main.setAgentUserId(100L);
        InsuranceApplyRecord child = new InsuranceApplyRecord();
        child.setOrderNo("BH001-0001");
        child.setBatchOrderNo("BH001");
        child.setCustomerName("张三");
        child.setStatus(3);
        InsuranceApplicationSignInvite pending = invite(1L, child.getOrderNo(), "PENDING");
        InsuranceApplicationSignInvite signed = invite(2L, child.getOrderNo(), "SIGNED");
        when(orders.selectOne(any())).thenReturn(main);
        when(orders.selectList(any())).thenReturn(List.of(child));
        when(publicSignService.findOwnedInvites("BH001")).thenReturn(List.of(pending, signed));
        when(publicSignService.issueLink(1L, child)).thenReturn(Map.of(
            "url", "https://m.example.com/sign?token=abc", "expiresAt", "2026-09-20"));

        try (var login = mockStatic(LoginHelper.class)) {
            login.when(LoginHelper::getUserId).thenReturn(100L);
            Map<String, Object> result = service.issueAllLinks("BH001");
            assertEquals(1, result.get("count"));
            List<?> links = (List<?>) result.get("links");
            assertEquals(1, links.size());
            assertEquals("https://m.example.com/sign?token=abc", ((Map<?, ?>) links.get(0)).get("url"));
        }
        verify(publicSignService).issueLink(1L, child);
        verify(publicSignService, never()).issueLink(2L, child);
    }

    private InsuranceApplicationSignInvite invite(Long id, String orderNo, String status) {
        InsuranceApplicationSignInvite invite = new InsuranceApplicationSignInvite();
        invite.setId(id);
        invite.setOrderNo(orderNo);
        invite.setSignerName("签署人");
        invite.setSignerRole("APPLICANT");
        invite.setStatus(status);
        return invite;
    }

    private SignedBatchTemplateDescriptor.Descriptor descriptor(String hash) {
        SignedBatchTemplateDescriptor.Descriptor descriptor = mock(SignedBatchTemplateDescriptor.Descriptor.class);
        when(descriptor.getSchemaHash()).thenReturn(hash);
        when(descriptor.getDynamicFields()).thenReturn(List.of());
        return descriptor;
    }

    private BatchSubmitDTO submission() {
        BatchSubmitDTO submit = new BatchSubmitDTO();
        submit.setProductId(1L);
        submit.setPolicyStartDate(LocalDate.now().plusDays(2).toString());
        submit.setTemplateMode(SignedBatchTemplateDescriptor.MODE);
        submit.setTemplateVersion(SignedBatchTemplateDescriptor.VERSION);
        submit.setSchemaHash("schema-hash");
        return submit;
    }

    private SignedBatchInsuredImportDto applicantOnlyRow() {
        SignedBatchInsuredImportDto row = new SignedBatchInsuredImportDto();
        row.setAppName("张三");
        row.setAppCertType("0");
        row.setAppCertNo("11010519491231002X");
        row.setAppCertStartDate("2020-01-01");
        row.setAppCertEndDate("2030-01-01");
        row.setAppPhone("13800138000");
        row.setAppRegion("北京市朝阳区");
        row.setAppAddress("示例路1号");
        row.setRelation("0");
        return row;
    }
}
