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
    @DisplayName("各角色有效费率分别取整后累加")
    void shouldSumRoundedRoleRates() {
        BigDecimal baseRate = new BigDecimal("0.135");
        BigDecimal salesRate = CommissionCalculationUtils.calculateEffectiveRate(baseRate, new BigDecimal("0.85"));
        BigDecimal teamRate = CommissionCalculationUtils.calculateEffectiveRate(baseRate, new BigDecimal("0.10"));
        BigDecimal projectRate = CommissionCalculationUtils.calculateEffectiveRate(baseRate, new BigDecimal("0.05"));

        assertEquals(new BigDecimal("0.11"), salesRate);
        assertEquals(new BigDecimal("0.01"), teamRate);
        assertEquals(new BigDecimal("0.00"), projectRate);
        assertEquals(new BigDecimal("0.12"), salesRate.add(teamRate).add(projectRate));
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
        BigDecimal premium = new BigDecimal("123.45");
        BigDecimal effectiveRate = CommissionCalculationUtils.calculateEffectiveRate(
            new BigDecimal("0.135"), new BigDecimal("0.85"));

        BigDecimal netPremium = premium.multiply(BigDecimal.ONE.subtract(effectiveRate))
            .setScale(2, RoundingMode.HALF_UP);

        assertEquals(new BigDecimal("109.87"), netPremium);
    }
}
