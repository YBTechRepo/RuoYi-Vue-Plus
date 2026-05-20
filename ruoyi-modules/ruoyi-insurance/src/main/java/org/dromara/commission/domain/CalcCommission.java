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
    private String productName;

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
    private BigDecimal netPremium;

    /**
     * 支付方式 (1-净费，2-全额)
     */
    private Integer paymentMode;

    /**
     * 真实的付款人/操作人 ID (用于下游判断谁已经享受了抵扣)
     */
    private Long payerUserId;

    /**
     * 业务来源：1-投保申请记录 2-保单记录
     */
    private Integer bizSource;

    //private Long projectDeptId;
}
