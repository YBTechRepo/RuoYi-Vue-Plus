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

    //private Long projectDeptId;
}
