package org.dromara.insurance.service;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.insurance.domain.*;
import org.dromara.insurance.domain.vo.InsuranceApplyRecordVo;
import org.dromara.insurance.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("dev")
class ApplicationFormGuardTest {
    private InsuranceApplicationDocumentMapper documents;
    private InsuranceApplyRecordMapper orders;
    private InsuranceOrderApplicantMapper applicants;
    private InsuranceOrderInsuredMapper insureds;
    private ApplicationFormGuard guard;
    private InsuranceApplyRecord order;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void setup() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), "test"), InsuranceApplicationDocument.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), "test"), InsuranceApplyRecord.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), "test"), InsuranceOrderApplicant.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), "test"), InsuranceOrderInsured.class);
        documents = mock(InsuranceApplicationDocumentMapper.class);
        orders = mock(InsuranceApplyRecordMapper.class);
        applicants = mock(InsuranceOrderApplicantMapper.class);
        insureds = mock(InsuranceOrderInsuredMapper.class);
        guard = spy(new ApplicationFormGuard(mock(InsuranceProductConfigMapper.class), orders, documents, applicants, insureds, json));
        order = new InsuranceApplyRecord();
        order.setApplicationFormRequired(true);
        order.setOrderNo("ORDER-001");
        order.setProductId(1L);
        order.setProductMode(1);
        order.setInsureMode(1);
        order.setPaymentMode(1);
        order.setStatus(3);
        order.setAgentUserId(10L);
        order.setPremium(new BigDecimal("100.00"));
        order.setPolicyStartDate(Date.from(LocalDate.now().plusDays(2).atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant()));
    }

    @Test
    void disabledProductsAndHistoricPaidOrdersDoNotRequireSigning() {
        order.setApplicationFormRequired(false);
        order.setStatus(0);
        assertDoesNotThrow(() -> guard.assertReady(order));
        verify(guard, never()).product(any());
        order.setStatus(3);
        var product = new InsuranceProductConfig();
        product.setApplicationFormRequired(false);
        doReturn(product).when(guard).product(1L);
        assertDoesNotThrow(() -> guard.assertReady(order));
    }

    @Test
    void enablingProductAlsoRequiresSigningForExistingUnpaidOrder() {
        order.setApplicationFormRequired(false);
        var product = new InsuranceProductConfig();
        product.setApplicationFormRequired(true);
        doReturn(product).when(guard).product(1L);
        assertTrue(guard.required(order));
        assertThrows(ServiceException.class, () -> guard.assertReady(order));
    }

    @Test
    void rejectsBatchSubmissionOnlyWhenEnabled() {
        var product = new InsuranceProductConfig();
        product.setApplicationFormRequired(true);
        doReturn(product).when(guard).product(1L);
        assertThrows(ServiceException.class, () -> guard.assertBatchAllowed(1L));
        product.setApplicationFormRequired(false);
        assertDoesNotThrow(() -> guard.assertBatchAllowed(1L));
    }

    @Test
    void signedBatchMainDoesNotNeedOwnDocumentButChildKeepsSnapshotRequirement() {
        order.setIsBatch(1);
        order.setApplicationFormRequired(false);
        var product = new InsuranceProductConfig();
        product.setApplicationFormRequired(true);
        doReturn(product).when(guard).product(1L);
        assertFalse(guard.required(order));
        assertDoesNotThrow(() -> guard.assertReady(order));

        order.setIsBatch(2);
        order.setApplicationFormRequired(true);
        product.setApplicationFormRequired(false);
        assertTrue(guard.required(order));
        assertThrows(ServiceException.class, () -> guard.assertReady(order));
    }

    @Test
    void rejectsMultiplePeopleWrongOwnerAndPaidEdits() {
        try (var login = mockStatic(LoginHelper.class)) {
            login.when(LoginHelper::getUserId).thenReturn(10L);
            assertThrows(ServiceException.class, () -> guard.beforeSave(order, 2));
            order.setStatus(0);
            assertThrows(ServiceException.class, () -> guard.beforeSave(order, 1));
            order.setStatus(3);
            login.when(LoginHelper::getUserId).thenReturn(20L);
            assertThrows(ServiceException.class, () -> guard.beforeSave(order, 1));
            verifyNoInteractions(documents);
        }
    }

    @Test
    void editingUnpaidInformationInvalidatesPriorSignature() {
        try (var login = mockStatic(LoginHelper.class)) {
            login.when(LoginHelper::getUserId).thenReturn(10L);
            guard.beforeSave(order, 1);
            verify(documents).update(isNull(), any());
        }
    }

    @Test
    void paymentRequiresReadyFileAndUnchangedSource() throws Exception {
        assertThrows(ServiceException.class, () -> guard.assertReady(order));
        var document = new InsuranceApplicationDocument();
        document.setStatus("FAILED");
        when(documents.selectOne(any())).thenReturn(document);
        assertThrows(ServiceException.class, () -> guard.assertReady(order));
        document.setStatus("READY");
        document.setPdfKey("insurance-applications/example.pdf");
        doReturn("before").when(guard).sourceHash(order);
        document.setSnapshotJson(json.writeValueAsString(Map.of("sourceHash", "before")));
        assertDoesNotThrow(() -> guard.assertReady(order));
        doReturn("after").when(guard).sourceHash(order);
        assertThrows(ServiceException.class, () -> guard.assertReady(order));
    }

    @Test
    void rejectsExpiredStartDateEvenWhenDocumentIsReady() throws Exception {
        var document = new InsuranceApplicationDocument();
        document.setStatus("READY");
        document.setPdfKey("insurance-applications/example.pdf");
        document.setSnapshotJson(json.writeValueAsString(Map.of("sourceHash", "same")));
        when(documents.selectOne(any())).thenReturn(document);
        doReturn("same").when(guard).sourceHash(order);
        order.setPolicyStartDate(new Date(0));
        assertThrows(ServiceException.class, () -> guard.assertReady(order));
        order.setStatus(4);
        assertThrows(ServiceException.class, () -> guard.assertReady(order));
    }

    @Test
    void sourceHashIncludesPersonalDetailsButNotSettlementNetPremium() {
        var applicant = new InsuranceOrderApplicant();
        applicant.setApplicantName("测试家长");
        when(applicants.selectOne(any())).thenReturn(applicant);
        when(insureds.selectList(any())).thenReturn(List.of());
        String initial = guard.sourceHash(order);
        order.setNetPremium(new BigDecimal("80.00"));
        assertEquals(initial, guard.sourceHash(order));
        applicant.setApplicantName("其他家长");
        assertNotEquals(initial, guard.sourceHash(order));
    }

    @Test
    void platformEnrichmentKeepsSameOrderNumberSeparatedByTenant() {
        var tenantA = new InsuranceApplyRecordVo();
        tenantA.setTenantId("tenant-a");
        tenantA.setOrderNo("SAME-ORDER");
        tenantA.setApplicationFormRequired(true);
        var tenantB = new InsuranceApplyRecordVo();
        tenantB.setTenantId("tenant-b");
        tenantB.setOrderNo("SAME-ORDER");
        tenantB.setApplicationFormRequired(true);

        var ready = new InsuranceApplicationDocument();
        ready.setId(2L);
        ready.setTenantId("tenant-a");
        ready.setOrderNo("SAME-ORDER");
        ready.setStatus("READY");
        var failed = new InsuranceApplicationDocument();
        failed.setId(1L);
        failed.setTenantId("tenant-b");
        failed.setOrderNo("SAME-ORDER");
        failed.setStatus("FAILED");
        when(documents.selectList(any())).thenReturn(List.of(ready, failed));

        guard.enrichPlatform(List.of(tenantA, tenantB));

        assertEquals("READY", tenantA.getApplicationFormStatus());
        assertEquals("FAILED", tenantB.getApplicationFormStatus());
    }

    @Test
    void platformEnrichmentAggregatesReadyChildDocumentsForBatchMain() {
        var main = new InsuranceApplyRecordVo();
        main.setTenantId("tenant-a");
        main.setOrderNo("BATCH-001");
        main.setIsBatch(1);
        main.setStatus(0);
        main.setApplicationFormRequired(false);

        var child1 = batchChild(11L, "BATCH-001-0001");
        var child2 = batchChild(12L, "BATCH-001-0002");
        when(orders.selectList(any())).thenReturn(List.of(child1, child2));

        var ready1 = readyDocument(21L, "BATCH-001-0001");
        var ready2 = readyDocument(22L, "BATCH-001-0002");
        when(documents.selectList(any())).thenReturn(List.of(ready1, ready2));

        guard.enrichPlatform(List.of(main));

        assertTrue(main.getApplicationFormRequired());
        assertEquals("READY", main.getApplicationFormStatus());
    }

    private InsuranceApplyRecord batchChild(Long id, String orderNo) {
        var child = new InsuranceApplyRecord();
        child.setId(id);
        child.setTenantId("tenant-a");
        child.setOrderNo(orderNo);
        child.setBatchOrderNo("BATCH-001");
        child.setIsBatch(2);
        child.setStatus(0);
        child.setApplicationFormRequired(true);
        return child;
    }

    private InsuranceApplicationDocument readyDocument(Long id, String orderNo) {
        var document = new InsuranceApplicationDocument();
        document.setId(id);
        document.setTenantId("tenant-a");
        document.setOrderNo(orderNo);
        document.setStatus("READY");
        return document;
    }
}
