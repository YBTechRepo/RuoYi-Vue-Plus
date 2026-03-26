package org.dromara.commission.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommissionSummaryVo {
    /**
     * 累计总收益
     */
    private BigDecimal totalAmount;

    /**
     * 月度预估收益 (根据查询月份动态变动)
     */
    private BigDecimal monthAmount;
}
