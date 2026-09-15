package org.dromara.commission.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 佣金计算工具。
 */
public final class CommissionCalculationUtils {

    private static final int RATE_SCALE = 2;
    private static final int MONEY_SCALE = 2;

    private CommissionCalculationUtils() {
    }

    /**
     * 计算角色有效费率，并向下取整到整数百分比。
     *
     * @param baseRate 基础佣金费率
     * @param roleRatio 角色分配比例
     * @return 取整后的角色有效费率，例如 0.11475 返回 0.11
     */
    public static BigDecimal calculateEffectiveRate(BigDecimal baseRate, BigDecimal roleRatio) {
        if (baseRate == null || roleRatio == null) {
            return BigDecimal.ZERO;
        }
        return baseRate.multiply(roleRatio).setScale(RATE_SCALE, RoundingMode.DOWN);
    }

    /**
     * 按取整后的角色有效费率计算佣金金额，金额四舍五入保留两位小数。
     */
    public static BigDecimal calculateCommissionAmount(BigDecimal premium, BigDecimal baseRate, BigDecimal roleRatio) {
        if (premium == null) {
            return BigDecimal.ZERO;
        }
        return calculateAmount(premium, calculateEffectiveRate(baseRate, roleRatio));
    }

    /**
     * 计算三层角色有效费率，费率尾差统一归项目负责人。
     */
    public static RoleRateResult calculateRoleRates(BigDecimal baseRate, BigDecimal salesRatio,
                                                    BigDecimal teamRatio, BigDecimal projectRatio) {
        BigDecimal normalizedSalesRatio = defaultZero(salesRatio);
        BigDecimal normalizedTeamRatio = defaultZero(teamRatio);
        BigDecimal normalizedProjectRatio = defaultZero(projectRatio);

        BigDecimal salesRate = calculateEffectiveRate(baseRate, normalizedSalesRatio);
        BigDecimal teamRate = calculateEffectiveRate(baseRate, normalizedTeamRatio);
        BigDecimal totalRate = calculateEffectiveRate(baseRate,
            normalizedSalesRatio.add(normalizedTeamRatio).add(normalizedProjectRatio));
        BigDecimal projectRate = totalRate.subtract(salesRate).subtract(teamRate);

        return new RoleRateResult(salesRate, teamRate, projectRate, totalRate);
    }

    /**
     * 按三层角色有效费率计算佣金金额，金额尾差统一归项目负责人。
     */
    public static RoleAmountResult calculateRoleAmounts(BigDecimal premium, RoleRateResult rateResult) {
        if (premium == null || rateResult == null) {
            return new RoleAmountResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal totalAmount = calculateAmount(premium, rateResult.totalRate());
        BigDecimal salesAmount = calculateAmount(premium, rateResult.salesRate());
        BigDecimal teamAmount = calculateAmount(premium, rateResult.teamRate());
        BigDecimal projectAmount = totalAmount.subtract(salesAmount).subtract(teamAmount);

        return new RoleAmountResult(salesAmount, teamAmount, projectAmount, totalAmount);
    }

    private static BigDecimal calculateAmount(BigDecimal premium, BigDecimal effectiveRate) {
        return premium.multiply(effectiveRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public record RoleRateResult(BigDecimal salesRate, BigDecimal teamRate,
                                 BigDecimal projectRate, BigDecimal totalRate) {

        public BigDecimal salesDisplayRate() {
            return salesRate;
        }

        public BigDecimal teamDisplayRate() {
            return salesRate.add(teamRate);
        }

        public BigDecimal projectDisplayRate() {
            return totalRate;
        }
    }

    public record RoleAmountResult(BigDecimal salesAmount, BigDecimal teamAmount,
                                   BigDecimal projectAmount, BigDecimal totalAmount) {
    }
}
