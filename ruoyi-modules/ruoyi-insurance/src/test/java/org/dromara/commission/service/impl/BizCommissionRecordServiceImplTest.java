package org.dromara.commission.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.spring.SpringUtil;
import io.github.linpeilie.Converter;
import org.dromara.commission.domain.BizCommissionDept;
import org.dromara.commission.domain.BizCommissionProduct;
import org.dromara.commission.domain.BizCommissionRecord;
import org.dromara.commission.domain.CalcCommission;
import org.dromara.commission.mapper.BizCommissionRecordMapper;
import org.dromara.commission.service.IBizCommissionDeptService;
import org.dromara.commission.service.IBizCommissionProductService;
import org.dromara.insurance.domain.InsuranceProductCommission;
import org.dromara.insurance.mapper.InsuranceApplyRecordMapper;
import org.dromara.insurance.mapper.InsurancePolicyMapper;
import org.dromara.insurance.service.IInsuranceProductCommissionService;
import org.dromara.system.domain.vo.SysDeptVo;
import org.dromara.system.service.ISysDeptService;
import org.dromara.system.service.ISysUserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
@ExtendWith(MockitoExtension.class)
class BizCommissionRecordServiceImplTest {

    private static final Long DEPT_ID = 10L;
    private static final String TENANT_ID = "205387";

    @Mock
    private BizCommissionRecordMapper baseMapper;
    @Mock
    private IInsuranceProductCommissionService insuranceProductCommissionService;
    @Mock
    private IBizCommissionProductService bizCommissionProductService;
    @Mock
    private IBizCommissionDeptService bizCommissionDeptService;
    @Mock
    private ISysDeptService sysDeptService;
    @Mock
    private ISysUserService sysUserService;
    @Mock
    private InsuranceApplyRecordMapper insuranceApplyRecordMapper;
    @Mock
    private InsurancePolicyMapper insurancePolicyMapper;

    @InjectMocks
    private BizCommissionRecordServiceImpl service;

    @BeforeAll
    static void initializeMapstructConverter() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        Converter converter = new Converter() {
            @Override
            public <S, T> T convert(S source, Class<T> targetType) {
                return BeanUtil.copyProperties(source, targetType);
            }
        };
        beanFactory.registerSingleton("testMapstructConverter", converter);
        new SpringUtil().postProcessBeanFactory(beanFactory);
    }

    @BeforeEach
    void setUp() {
        InsuranceProductCommission productCommission = new InsuranceProductCommission();
        productCommission.setStatus(0);
        productCommission.setCommissionRate(new BigDecimal("0.24"));
        when(insuranceProductCommissionService.queryByProductIdAndTenantId(20L, TENANT_ID))
            .thenReturn(productCommission);

        SysDeptVo dept = new SysDeptVo();
        dept.setDeptId(DEPT_ID);
        dept.setAncestors("0");
        lenient().when(sysDeptService.selectDeptById(DEPT_ID)).thenReturn(dept);

        BizCommissionDept commissionDept = new BizCommissionDept();
        commissionDept.setStatus(0);
        commissionDept.setSalesRatio(new BigDecimal("0.70"));
        commissionDept.setTeamRatio(new BigDecimal("0.20"));
        commissionDept.setProjectRatio(new BigDecimal("0.10"));
        lenient().when(bizCommissionDeptService.queryByDeptIdAndTenantId(DEPT_ID, TENANT_ID))
            .thenReturn(commissionDept);

        when(baseMapper.insert(any(BizCommissionRecord.class))).thenReturn(1);
    }

    @Test
    @DisplayName("机构策略的费率与金额尾差归项目负责人")
    void shouldAssignRemainderToProjectLeaderForDeptStrategy() {
        service.calcCommission(buildCalcCommission(101L, 102L, 103L));

        BizCommissionRecord record = captureInsertedRecord();
        assertCommissionAmounts(record, "16.00", "4.00", "4.01");
        assertEquals(new BigDecimal("24.01"), record.getCommissionBase());
        assertEquals(1, record.getCalcStrategy());
    }

    @Test
    @DisplayName("特殊产品策略复用相同尾差规则")
    void shouldAssignRemainderToProjectLeaderForProductStrategy() {
        BizCommissionProduct specialProduct = new BizCommissionProduct();
        specialProduct.setStatus(0);
        specialProduct.setSalesRatio(new BigDecimal("0.70"));
        specialProduct.setTeamRatio(new BigDecimal("0.20"));
        specialProduct.setProjectRatio(new BigDecimal("0.10"));
        when(bizCommissionProductService.queryByProductIdAndTenantId(20L, TENANT_ID))
            .thenReturn(specialProduct);

        service.calcCommission(buildCalcCommission(101L, 102L, 103L));

        BizCommissionRecord record = captureInsertedRecord();
        assertCommissionAmounts(record, "16.00", "4.00", "4.01");
        assertEquals(0, record.getCalcStrategy());
    }

    @Test
    @DisplayName("缺少团队负责人时团队佣金向上翻滚且总额不变")
    void shouldRollTeamAmountUpWithoutChangingTotal() {
        service.calcCommission(buildCalcCommission(101L, null, 103L));

        BizCommissionRecord record = captureInsertedRecord();
        assertCommissionAmounts(record, "16.00", "0.00", "8.01");
    }

    @Test
    @DisplayName("三层身份重叠时全部佣金归项目负责人且总额不变")
    void shouldRollAllAmountsUpForOverlappingRoles() {
        service.calcCommission(buildCalcCommission(103L, 103L, 103L));

        BizCommissionRecord record = captureInsertedRecord();
        assertCommissionAmounts(record, "0.00", "0.00", "24.01");
    }

    private CalcCommission buildCalcCommission(Long salesUserId, Long teamUserId, Long projectUserId) {
        CalcCommission calcCommission = new CalcCommission();
        calcCommission.setPolicyId(1L);
        calcCommission.setPolicyNo("POLICY-001");
        calcCommission.setProductId(20L);
        calcCommission.setProductName("测试产品");
        calcCommission.setTenantId(TENANT_ID);
        calcCommission.setCreateDeptId(DEPT_ID);
        calcCommission.setCreateById(salesUserId);
        calcCommission.setPolicyPremium(new BigDecimal("100.03"));
        calcCommission.setNetPremium(new BigDecimal("100.03"));
        calcCommission.setPaymentMode(2);
        calcCommission.setSalesUserId(salesUserId);
        calcCommission.setTeamUserId(teamUserId);
        calcCommission.setProjectUserId(projectUserId);
        calcCommission.setSalesUserName("业务员");
        calcCommission.setTeamUserName(teamUserId == null ? "" : "团队负责人");
        calcCommission.setProjectUserName("项目负责人");
        return calcCommission;
    }

    private BizCommissionRecord captureInsertedRecord() {
        ArgumentCaptor<BizCommissionRecord> captor = ArgumentCaptor.forClass(BizCommissionRecord.class);
        verify(baseMapper).insert(captor.capture());
        return captor.getValue();
    }

    private void assertCommissionAmounts(BizCommissionRecord record, String sales, String team, String project) {
        assertEquals(0, new BigDecimal(sales).compareTo(record.getSalesAmount()));
        assertEquals(0, new BigDecimal(team).compareTo(record.getTeamAmount()));
        assertEquals(0, new BigDecimal(project).compareTo(record.getProjectAmount()));
        assertEquals(0, record.getCommissionBase().compareTo(totalAmount(record)));
    }

    private BigDecimal totalAmount(BizCommissionRecord record) {
        return record.getSalesAmount().add(record.getTeamAmount()).add(record.getProjectAmount());
    }
}
