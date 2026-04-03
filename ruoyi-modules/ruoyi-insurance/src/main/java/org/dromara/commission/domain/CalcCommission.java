package org.dromara.commission.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalcCommission {
    private Long policyId;
    private String policyNo;
    private Long productId;

    private String salesUserName;
    private String teamUserName;
    private String projectUserName;
    private Long salesUserId;
    private Long teamUserId;
    private Long projectUserId;

    private Long createById;
    private String tenantId;
    private Long createDeptId;

    private BigDecimal policyPremium;

    /**
     * 支付方式 (1-净费，2-全额)
     */
    private Integer paymentMode;

    /**
     * 真实的付款人/操作人 ID (用于下游判断谁已经享受了抵扣)
     */
    private Long payerUserId;

    //private Long projectDeptId;
}
