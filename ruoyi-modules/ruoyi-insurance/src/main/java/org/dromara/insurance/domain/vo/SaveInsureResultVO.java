package org.dromara.insurance.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaveInsureResultVO {
    private String orderNo;
    private BigDecimal premium;
}
