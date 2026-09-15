package org.dromara.insurance.service.impl;

import org.dromara.commission.event.PolicyUnderwrittenEvent;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.insurance.domain.InsuranceApplyRecord;
import org.dromara.insurance.domain.InsuranceProductChannelMapping;
import org.dromara.insurance.domain.InsuranceProductConfig;
import org.dromara.insurance.domain.bo.InsurancePolicyBo;
import org.dromara.insurance.domain.dto.AppntDto;
import org.dromara.insurance.domain.dto.InsuredDto;
import org.dromara.insurance.domain.dto.PolicyCallbackDto;
import org.dromara.insurance.domain.dto.PolicyDto;
import org.dromara.insurance.mapper.InsuranceApplyRecordMapper;
import org.dromara.insurance.mapper.InsuranceProductChannelMappingMapper;
import org.dromara.insurance.mapper.InsuranceProductConfigMapper;
import org.dromara.insurance.mapper.InsuranceTenantProductMapper;
import org.dromara.insurance.service.IInsurancePolicyService;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysDeptService;
import org.dromara.system.service.ISysUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
@ExtendWith(MockitoExtension.class)
class OpenPolicyFacadeServiceImplTest {

    @Mock
    private IInsurancePolicyService insurancePolicyService;
    @Mock
    private InsuranceProductConfigMapper productConfigMapper;
    @Mock
    private InsuranceProductChannelMappingMapper channelMappingMapper;
    @Mock
    private InsuranceTenantProductMapper tenantProductMapper;
    @Mock
    private InsuranceApplyRecordMapper applyRecordMapper;
    @Mock
    private ISysUserService sysUserService;
    @Mock
    private ISysDeptService sysDeptService;
    @Mock
    private ApplicationContext applicationContext;

    private OpenPolicyFacadeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OpenPolicyFacadeServiceImpl(
            insurancePolicyService,
            productConfigMapper,
            channelMappingMapper,
            tenantProductMapper,
            applyRecordMapper,
            sysUserService,
            sysDeptService,
            applicationContext
        );
    }

    @AfterEach
    void clearTenantContext() {
        TenantHelper.clearDynamic();
    }

    @Test
    @DisplayName("渠道产品编码映射到平台产品")
    void shouldResolveMappedChannelProduct() {
        PolicyDto policy = new PolicyDto();
        policy.setCompanyType("channel-a");
        policy.setProductPlanCode("RIDER-001");

        InsuranceProductChannelMapping mapping = new InsuranceProductChannelMapping();
        mapping.setId(10L);
        mapping.setProductId(20L);
        InsuranceProductConfig product = buildProduct(20L, "PRODUCT-A");

        when(productConfigMapper.selectOne(any())).thenReturn(null);
        when(channelMappingMapper.selectOne(any())).thenReturn(mapping);
        when(productConfigMapper.selectById(20L)).thenReturn(product);

        InsuranceProductConfig result = service.resolveProduct(policy, null);

        assertEquals(20L, result.getId());
        assertEquals("PRODUCT-A", result.getProductCode());
    }

    @Test
    @DisplayName("未知渠道产品编码明确失败")
    void shouldRejectUnknownChannelProduct() {
        PolicyDto policy = new PolicyDto();
        policy.setCompanyType("channel-a");
        policy.setProductPlanCode("UNKNOWN");

        when(productConfigMapper.selectOne(any())).thenReturn(null);
        when(channelMappingMapper.selectOne(any())).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.resolveProduct(policy, null));

        assertEquals("未配置渠道产品映射：channel-a/UNKNOWN", exception.getMessage());
    }

    @Test
    @DisplayName("关联投保订单的回调只保存保单不重复计算佣金")
    void shouldNotCalculateCommissionAgainForApplyOrder() {
        PolicyCallbackDto callback = buildCallback("POLICY-001", "ORDER-001", "88.00", "SOURCE-001");
        SysUserVo salesUser = buildSalesUser(false);
        InsuranceApplyRecord applyRecord = new InsuranceApplyRecord();
        applyRecord.setOrderNo("ORDER-001");
        applyRecord.setProductId(20L);
        applyRecord.setCommissionStatus(0);
        InsuranceProductConfig product = buildProduct(20L, "PRODUCT-A");

        when(sysUserService.selectUserById(100L)).thenReturn(salesUser);
        when(insurancePolicyService.queryByPolicyNoAndTenantId("POLICY-001", "100001")).thenReturn(null);
        when(applyRecordMapper.selectOne(any())).thenReturn(applyRecord);
        when(productConfigMapper.selectById(20L)).thenReturn(product);
        when(insurancePolicyService.insertByBo(any())).thenAnswer(invocation -> {
            InsurancePolicyBo bo = invocation.getArgument(0);
            bo.setId(30L);
            return true;
        });

        service.processCallback(callback);

        ArgumentCaptor<InsurancePolicyBo> policyCaptor = ArgumentCaptor.forClass(InsurancePolicyBo.class);
        verify(insurancePolicyService).insertByBo(policyCaptor.capture());
        InsurancePolicyBo savedPolicy = policyCaptor.getValue();
        assertEquals("ORDER-001", savedPolicy.getOrderNo());
        assertEquals(20L, savedPolicy.getProductId());
        assertEquals("SOURCE-001", savedPolicy.getProductCode());
        assertEquals("渠道产品", savedPolicy.getProductName());
        assertEquals("SOURCE-001", savedPolicy.getSourceProductCode());
        assertEquals(0, savedPolicy.getCommissionStatus());
        verify(applicationContext, never()).publishEvent(any(PolicyUnderwrittenEvent.class));
    }

    @Test
    @DisplayName("订单号等于保单号的外部保单按自身保费计算佣金")
    void shouldCalculateExternalPolicyByItsOwnPremium() {
        PolicyCallbackDto callback = buildCallback("POLICY-002", "POLICY-002", "20.00", "RIDER-001");
        SysUserVo salesUser = buildSalesUser(true);
        InsuranceProductChannelMapping mapping = new InsuranceProductChannelMapping();
        mapping.setProductId(20L);
        InsuranceProductConfig product = buildProduct(20L, "PRODUCT-A");
        SysDeptVo dept = new SysDeptVo();
        dept.setDeptCategory("1");

        when(sysUserService.selectUserById(100L)).thenReturn(salesUser);
        when(insurancePolicyService.queryByPolicyNoAndTenantId("POLICY-002", "100001")).thenReturn(null);
        when(applyRecordMapper.selectOne(any())).thenReturn(null);
        when(productConfigMapper.selectOne(any())).thenReturn(null);
        when(channelMappingMapper.selectOne(any())).thenReturn(mapping);
        when(productConfigMapper.selectById(20L)).thenReturn(product);
        when(sysDeptService.selectDeptById(10L)).thenReturn(dept);
        when(insurancePolicyService.insertByBo(any())).thenAnswer(invocation -> {
            InsurancePolicyBo bo = invocation.getArgument(0);
            bo.setId(31L);
            return true;
        });

        service.processCallback(callback);

        ArgumentCaptor<InsurancePolicyBo> policyCaptor = ArgumentCaptor.forClass(InsurancePolicyBo.class);
        verify(insurancePolicyService).insertByBo(policyCaptor.capture());
        InsurancePolicyBo savedPolicy = policyCaptor.getValue();
        assertEquals(20L, savedPolicy.getProductId());
        assertEquals("RIDER-001", savedPolicy.getProductCode());
        assertEquals("渠道产品", savedPolicy.getProductName());

        ArgumentCaptor<PolicyUnderwrittenEvent> eventCaptor = ArgumentCaptor.forClass(PolicyUnderwrittenEvent.class);
        verify(applicationContext).publishEvent(eventCaptor.capture());
        assertEquals("POLICY-002", eventCaptor.getValue().getCalcParam().getPolicyNo());
        assertEquals(new BigDecimal("20.00"), eventCaptor.getValue().getCalcParam().getPolicyPremium());
    }

    private PolicyCallbackDto buildCallback(String policyNo, String orderNo, String premium, String productCode) {
        PolicyDto policy = new PolicyDto();
        policy.setPolicyNo(policyNo);
        policy.setOrderNo(orderNo);
        policy.setPrem(premium);
        policy.setAmt("");
        policy.setCompanyType("channel-a");
        policy.setProductPlanCode(productCode);
        policy.setProductPlanName("渠道产品");
        policy.setAgentCode("KD100");
        policy.setAppntDate("2026-08-21 10:00:00");
        policy.setAcceptDate("2026-08-21 10:00:00");
        policy.setPolicyStartDate("2026-08-22");
        policy.setPolicyEndDate("2027-08-21");

        AppntDto appnt = new AppntDto();
        appnt.setName("投保人");
        appnt.setSex("0");
        appnt.setIdNo("130000000000000001");
        appnt.setMobile("13800000000");

        InsuredDto insured = new InsuredDto();
        insured.setName("被保人");
        insured.setSex("0");
        insured.setIdNo("130000000000000002");
        insured.setMobile("13800000001");

        PolicyCallbackDto callback = new PolicyCallbackDto();
        callback.setPolicy(policy);
        callback.setAppnt(appnt);
        callback.setInsureds(List.of(insured));
        return callback;
    }

    private SysUserVo buildSalesUser(boolean withRole) {
        SysUserVo user = new SysUserVo();
        user.setUserId(100L);
        user.setDeptId(10L);
        user.setTenantId("100001");
        user.setNickName("测试业务员");
        if (withRole) {
            SysRoleVo role = new SysRoleVo();
            role.setRoleKey("projectleader");
            user.setRoles(List.of(role));
        }
        return user;
    }

    private InsuranceProductConfig buildProduct(Long id, String productCode) {
        InsuranceProductConfig product = new InsuranceProductConfig();
        product.setId(id);
        product.setProductCode(productCode);
        product.setProductName("平台产品A");
        return product;
    }
}
