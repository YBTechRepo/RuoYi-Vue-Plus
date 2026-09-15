package org.dromara.insurance.service.impl;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.dromara.insurance.domain.InsuranceApplyRecord;
import org.dromara.insurance.domain.InsurancePolicy;
import org.dromara.insurance.domain.bo.InsuranceProductConfigBo;
import org.dromara.insurance.domain.bo.InsuranceProductSaveBo;
import org.dromara.insurance.domain.dto.VoucherPdfResult;
import org.dromara.insurance.mapper.InsuranceApplyRecordMapper;
import org.dromara.insurance.mapper.InsurancePolicyMapper;
import org.dromara.insurance.service.IInsuranceProductConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Tag("dev")
@ExtendWith(MockitoExtension.class)
class InsurancePolicyVoucherPdfTest {

    private static final Long PRODUCT_ID = 100L;

    @Mock
    private InsuranceApplyRecordMapper applyRecordMapper;
    @Mock
    private InsurancePolicyMapper policyMapper;
    @Mock
    private IInsuranceProductConfigService productConfigService;

    private InsuranceApplyRecordServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InsuranceApplyRecordServiceImpl(
            applyRecordMapper,
            null,
            null,
            policyMapper,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            productConfigService
        );
    }

    @Test
    @DisplayName("关联订单的常规产品保单凭证隐藏保单保费")
    void shouldHidePremiumForRegularPolicyVoucherWithLinkedOrder() throws IOException {
        when(policyMapper.selectOne(any())).thenReturn(buildPolicy("P-001", "O-001"));
        when(applyRecordMapper.selectOne(any())).thenReturn(buildRecord("O-001", 2, null));

        String text = extractText(service.generateVoucherPdfByPolicyNo("P-001"));

        assertFalse(text.contains("保单保费"));
        assertTrue(text.contains("起保日期"));
        assertAgentNameHidden(text);
    }

    @Test
    @DisplayName("历史订单产品模式为空时按产品配置隐藏保单保费")
    void shouldFallbackToProductConfigForLinkedPolicyVoucher() throws IOException {
        when(policyMapper.selectOne(any())).thenReturn(buildPolicy("P-002", "O-002"));
        when(applyRecordMapper.selectOne(any())).thenReturn(buildRecord("O-002", null, PRODUCT_ID));
        when(productConfigService.getProductFull(PRODUCT_ID)).thenReturn(buildProductData(2));

        String text = extractText(service.generateVoucherPdfByPolicyNo("P-002"));

        assertFalse(text.contains("保单保费"));
        assertAgentNameHidden(text);
    }

    @Test
    @DisplayName("无关联订单时按保单产品配置隐藏保单保费")
    void shouldHidePremiumForRegularPolicyVoucherWithoutLinkedOrder() throws IOException {
        when(policyMapper.selectOne(any())).thenReturn(buildPolicy("P-003", null));
        when(productConfigService.getProductFull(PRODUCT_ID)).thenReturn(buildProductData(2));

        String text = extractText(service.generateVoucherPdfByPolicyNo("P-003"));

        assertFalse(text.contains("保单保费"));
        assertAgentNameHidden(text);
    }

    @Test
    @DisplayName("关联订单产品模式优先且非常规产品继续显示保单保费")
    void shouldKeepPremiumForNonRegularPolicyVoucher() throws IOException {
        when(policyMapper.selectOne(any())).thenReturn(buildPolicy("P-004", "O-004"));
        when(applyRecordMapper.selectOne(any())).thenReturn(buildRecord("O-004", 1, PRODUCT_ID));
        when(productConfigService.getProductFull(PRODUCT_ID)).thenReturn(buildProductData(2));

        String text = extractText(service.generateVoucherPdfByPolicyNo("P-004"));

        assertTrue(text.contains("保单保费"));
        assertAgentNameHidden(text);
    }

    @Test
    @DisplayName("投保记录入口的常规产品凭证继续显示保单保费")
    void shouldKeepPremiumForApplyRecordVoucher() throws IOException {
        when(applyRecordMapper.selectOne(any())).thenReturn(buildRecord("O-005", 2, null));

        String text = extractText(service.generateVoucherPdf("O-005"));

        assertTrue(text.contains("保单保费"));
        assertAgentNameHidden(text);
    }

    private InsurancePolicy buildPolicy(String policyNo, String orderNo) {
        InsurancePolicy policy = new InsurancePolicy();
        policy.setPolicyNo(policyNo);
        policy.setOrderNo(orderNo);
        policy.setProductId(PRODUCT_ID);
        policy.setProductCode("REGULAR-001");
        policy.setProductName("测试保险产品");
        policy.setAgentName("测试业务员");
        policy.setApplicantName("测试投保人");
        policy.setApplicantMobile("13800000000");
        policy.setInsuredName("测试被保人");
        policy.setPremium(new BigDecimal("199.00"));
        policy.setPolicyStartDate(new Date());
        policy.setAppntDate(new Date());
        return policy;
    }

    private InsuranceApplyRecord buildRecord(String orderNo, Integer productMode, Long productId) {
        InsuranceApplyRecord record = new InsuranceApplyRecord();
        record.setOrderNo(orderNo);
        record.setProductId(productId);
        record.setProductCode("REGULAR-001");
        record.setProductName("测试保险产品");
        record.setCustomerName("测试客户");
        record.setCustomerMobile("13800000000");
        record.setAgentName("测试业务员");
        record.setPremium(new BigDecimal("199.00"));
        record.setProductMode(productMode);
        record.setInsureMode(0);
        record.setIsBatch(0);
        record.setPolicyStartDate(new Date());
        record.setCreateTime(new Date());
        return record;
    }

    private InsuranceProductSaveBo buildProductData(Integer productMode) {
        InsuranceProductConfigBo product = new InsuranceProductConfigBo();
        product.setProductMode(productMode);
        InsuranceProductSaveBo productData = new InsuranceProductSaveBo();
        productData.setProduct(product);
        return productData;
    }

    private String extractText(VoucherPdfResult result) throws IOException {
        try (PDDocument document = PDDocument.load(result.getContent())) {
            return new PDFTextStripper().getText(document);
        }
    }

    private void assertAgentNameHidden(String text) {
        assertFalse(text.contains("业务员姓名"));
        assertFalse(text.contains("测试业务员"));
    }
}
