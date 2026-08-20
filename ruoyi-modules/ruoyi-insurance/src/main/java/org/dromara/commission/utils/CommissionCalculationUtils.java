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
        return premium.multiply(calculateEffectiveRate(baseRate, roleRatio))
            .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
