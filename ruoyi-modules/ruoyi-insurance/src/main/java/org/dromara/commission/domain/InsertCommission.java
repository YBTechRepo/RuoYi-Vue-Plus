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
}
