package org.dromara.insurance.domain.dto;

import lombok.Data;

@Data
public class PolicyDto {
    private String policyNo;
    private String orderNo;
    private String renewalPolicyNo;
    private String amt;
    private String prem;
    private String companyType;
    private String companyName;
    private String appntDate;
    private String acceptDate;
    private String productPlanCode;
    private String productPlanName;
    private String policyStartDate;
    private String policyEndDate;
    private String renewalType;
    private String agentCode;
}
