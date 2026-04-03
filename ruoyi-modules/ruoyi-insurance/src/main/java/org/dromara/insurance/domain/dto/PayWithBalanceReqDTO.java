package org.dromara.insurance.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayWithBalanceReqDTO {
    private String orderNo;
    private BigDecimal payAmount;
}
