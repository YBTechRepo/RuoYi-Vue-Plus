package org.dromara.commission.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("dev")
class CommissionCalculationUtilsTest {

    @Test
    @DisplayName("有效费率按整数百分比向下取整")
    void shouldRoundEffectiveRateDownToWholePercent() {
        BigDecimal result = CommissionCalculationUtils.calculateEffectiveRate(
            new BigDecimal("0.135"), new BigDecimal("0.85"));

        assertEquals(new BigDecimal("0.11"), result);
    }

    @Test
    @DisplayName("费率尾差统一归项目负责人")
    void shouldAssignRateRemainderToProjectLeader() {
        CommissionCalculationUtils.RoleRateResult result = CommissionCalculationUtils.calculateRoleRates(
            new BigDecimal("0.135"), new BigDecimal("0.85"),
            new BigDecimal("0.10"), new BigDecimal("0.05"));

        assertEquals(new BigDecimal("0.11"), result.salesRate());
        assertEquals(new BigDecimal("0.01"), result.teamRate());
        assertEquals(new BigDecimal("0.01"), result.projectRate());
        assertEquals(new BigDecimal("0.12"), result.teamDisplayRate());
        assertEquals(new BigDecimal("0.13"), result.projectDisplayRate());
    }

    @Test
    @DisplayName("24%按70% 20% 10%分配时项目负责人显示完整费率")
    void shouldKeepFullRateForProjectLeader() {
        CommissionCalculationUtils.RoleRateResult result = CommissionCalculationUtils.calculateRoleRates(
            new BigDecimal("0.24"), new BigDecimal("0.70"),
            new BigDecimal("0.20"), new BigDecimal("0.10"));

        assertEquals(new BigDecimal("0.16"), result.salesRate());
        assertEquals(new BigDecimal("0.04"), result.teamRate());
        assertEquals(new BigDecimal("0.04"), result.projectRate());
        assertEquals(new BigDecimal("0.16"), result.salesDisplayRate());
        assertEquals(new BigDecimal("0.20"), result.teamDisplayRate());
        assertEquals(new BigDecimal("0.24"), result.projectDisplayRate());
    }

    @Test
    @DisplayName("金额尾差统一归项目负责人")
    void shouldAssignAmountRemainderToProjectLeader() {
        CommissionCalculationUtils.RoleRateResult rateResult = CommissionCalculationUtils.calculateRoleRates(
            new BigDecimal("0.24"), new BigDecimal("0.70"),
            new BigDecimal("0.20"), new BigDecimal("0.10"));

        CommissionCalculationUtils.RoleAmountResult result = CommissionCalculationUtils.calculateRoleAmounts(
            new BigDecimal("100.03"), rateResult);

        assertEquals(new BigDecimal("16.00"), result.salesAmount());
        assertEquals(new BigDecimal("4.00"), result.teamAmount());
        assertEquals(new BigDecimal("4.01"), result.projectAmount());
        assertEquals(new BigDecimal("24.01"), result.totalAmount());
        assertEquals(result.totalAmount(), result.salesAmount().add(result.teamAmount()).add(result.projectAmount()));
    }

    @Test
    @DisplayName("空值和零费率返回零")
    void shouldReturnZeroForNullOrZeroRate() {
        assertEquals(0, CommissionCalculationUtils.calculateEffectiveRate(null, BigDecimal.ONE).compareTo(BigDecimal.ZERO));
        assertEquals(0, CommissionCalculationUtils.calculateEffectiveRate(BigDecimal.ONE, null).compareTo(BigDecimal.ZERO));
        assertEquals(0, CommissionCalculationUtils.calculateEffectiveRate(BigDecimal.ZERO, BigDecimal.ONE).compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("佣金金额按取整后费率四舍五入到分")
    void shouldCalculateCommissionAmountWithRoundedRate() {
        BigDecimal result = CommissionCalculationUtils.calculateCommissionAmount(
            new BigDecimal("123.45"), new BigDecimal("0.135"), new BigDecimal("0.85"));

        assertEquals(new BigDecimal("13.58"), result);
    }

    @Test
    @DisplayName("净费使用取整后的推广费率并保留到分")
    void shouldCalculateNetPremiumWithRoundedRate() {
        BigDecimal premium = new BigDecimal("100.00");
        CommissionCalculationUtils.RoleRateResult rateResult = CommissionCalculationUtils.calculateRoleRates(
            new BigDecimal("0.24"), new BigDecimal("0.70"),
            new BigDecimal("0.20"), new BigDecimal("0.10"));

        BigDecimal netPremium = premium.multiply(BigDecimal.ONE.subtract(rateResult.projectDisplayRate()))
            .setScale(2, RoundingMode.HALF_UP);

        assertEquals(new BigDecimal("76.00"), netPremium);
    }
}
