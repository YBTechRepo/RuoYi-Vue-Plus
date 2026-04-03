package org.dromara.commission.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InsertCommission {
    private CalcCommission calcCommission;
    private BigDecimal totalCommission;
    private BigDecimal salesCommission;
    private BigDecimal teamCommission;
    private BigDecimal projectCommission;
    private BigDecimal salesRatio;
    private BigDecimal teamRatio;
    private BigDecimal projectRatio;
    private Integer calcStrategy;

    /**
     * 业务员佣金发放状态 (1-正常发放，2-净费已前置抵扣)
     */
    private Integer salesStatus;

    /**
     * 团队长津贴发放状态 (1-正常发放，2-净费已前置抵扣)
     */
    private Integer teamStatus;

    /**
     * 总监津贴发放状态 (1-正常发放，2-净费已前置抵扣)
     */
    private Integer projectStatus;
}
