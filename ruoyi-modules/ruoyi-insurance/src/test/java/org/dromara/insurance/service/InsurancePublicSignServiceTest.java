package org.dromara.insurance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dromara.insurance.domain.InsuranceApplicationSignInvite;
import org.dromara.insurance.domain.InsuranceApplicationDocument;
import org.dromara.insurance.domain.InsuranceApplyRecord;
import org.dromara.insurance.domain.InsuranceOrderApplicant;
import org.dromara.insurance.domain.InsuranceOrderInsured;
import org.dromara.insurance.mapper.InsuranceApplicationDocumentMapper;
import org.dromara.insurance.mapper.InsuranceApplicationSignInviteMapper;
import org.dromara.insurance.mapper.InsuranceOrderApplicantMapper;
import org.dromara.insurance.mapper.InsuranceOrderInsuredMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("dev")
class InsurancePublicSignServiceTest {
    private InsuranceApplicationSignInviteMapper invites;
    private InsuranceApplicationDocumentMapper documents;
    private InsuranceOrderApplicantMapper applicants;
    private InsuranceOrderInsuredMapper insureds;
    private InsuranceApplicationFormService formService;
    private InsurancePublicSignService service;
    private InsuranceApplicationSignInvite invite;

    @BeforeEach
    void setup() {
        invites = mock(InsuranceApplicationSignInviteMapper.class);
        documents = mock(InsuranceApplicationDocumentMapper.class);
        applicants = mock(InsuranceOrderApplicantMapper.class);
        insureds = mock(InsuranceOrderInsuredMapper.class);
        formService = mock(InsuranceApplicationFormService.class);
        service = new InsurancePublicSignService(invites, documents, applicants, insureds,
            formService, new ObjectMapper());
        invite = new InsuranceApplicationSignInvite();
        invite.setId(1L);
        invite.setTenantId("100001");
        invite.setOrderNo("B-0001");
        invite.setDocumentId(9L);
        invite.setTokenHash(hash("link-token"));
        invite.setTokenExpiresAt(Date.from(Instant.now().plusSeconds(3600)));
        invite.setIdentitySalt("salt");
        invite.setIdentityHash(hash("salt:110101199001011234"));
        invite.setFailedAttempts(0);
        invite.setStatus("PENDING");
        invite.setSignerName("测试签署人");
        invite.setSignerRole("APPLICANT");
        invite.setSlotKeys("special,applicant");
        InsuranceOrderApplicant applicant = new InsuranceOrderApplicant();
        applicant.setApplicantCertType("0");
        when(applicants.selectOne(any())).thenReturn(applicant);
        when(invites.selectOne(any())).thenReturn(invite);
        when(invites.updateById(any(InsuranceApplicationSignInvite.class))).thenReturn(1);
    }

    @Test
    void linkTokenCanSignDirectlyWithoutIdentitySession() throws Exception {
        when(formService.signPublic(eq("B-0001"), any(), anyList(), anyMap(), eq("127.0.0.1"), eq("test")))
            .thenReturn(Map.of("status", "PARTIAL"));
        InsuranceApplicationDocument document = new InsuranceApplicationDocument();
        document.setId(9L);
        document.setTemplateCode("test");
        document.setTemplateVersion("v1");
        when(documents.selectById(any())).thenReturn(document);
        when(formService.metadata("test", "v1"))
            .thenReturn(new ObjectMapper().readTree("{\"statements\":[]}"));

        var result = service.sign("link-token", Map.of("special", new byte[]{1}), "127.0.0.1", "test");

        assertEquals("PARTIAL", result.get("status"));
        assertEquals("SIGNED", invite.getStatus());
        assertNotNull(invite.getSignedTime());
        verify(formService).signPublic(eq("B-0001"), any(), eq(List.of("special", "applicant")),
            anyMap(), eq("127.0.0.1"), eq("test"));
        verify(invites).updateById(invite);
    }

    @Test
    void issuedLinkExpiresAtPolicyStartWhenItIsSoonerThanSevenDays() {
        ReflectionTestUtils.setField(service, "publicSignUrl", "https://m.example.com/insurance/public-sign");
        when(invites.selectById(1L)).thenReturn(invite);
        InsuranceApplyRecord child = new InsuranceApplyRecord();
        child.setOrderNo(invite.getOrderNo());
        Instant policyStart = Instant.now().plusSeconds(2 * 24 * 3600);
        child.setPolicyStartDate(Date.from(policyStart));

        var result = service.issueLink(1L, child);
        assertTrue(result.get("url").toString().startsWith("https://m.example.com/insurance/public-sign?token="));
        assertEquals(Date.from(policyStart), invite.getTokenExpiresAt());
        verify(invites).updateById(invite);
    }

    @Test
    void adultDifferentFromApplicantCreatesTwoIndependentInvites() {
        InsuranceApplyRecord order = new InsuranceApplyRecord();
        order.setOrderNo("B-0001");
        order.setBatchOrderNo("B");
        InsuranceApplicationDocument document = new InsuranceApplicationDocument();
        document.setId(9L);
        document.setSnapshotJson("{\"insuredSignerRole\":\"被保险人\"}");
        InsuranceOrderApplicant applicant = new InsuranceOrderApplicant();
        applicant.setApplicantName("投保人");
        applicant.setApplicantCertNo("A123");
        InsuranceOrderInsured insured = new InsuranceOrderInsured();
        insured.setInsuredName("被保险人");
        insured.setInsuredCertNo("B456");

        service.createInvites(order, document, applicant, insured);
        var captor = org.mockito.ArgumentCaptor.forClass(InsuranceApplicationSignInvite.class);
        verify(invites, times(2)).insert(captor.capture());
        assertEquals(java.util.List.of("APPLICANT", "INSURED"),
            captor.getAllValues().stream().map(InsuranceApplicationSignInvite::getSignerRole).toList());
        assertEquals("special,applicant", captor.getAllValues().get(0).getSlotKeys());
        assertEquals("insured", captor.getAllValues().get(1).getSlotKeys());
    }

    private String hash(String value) {
        return ApplicationFormTemplate.hash(value.getBytes(StandardCharsets.UTF_8));
    }
}
