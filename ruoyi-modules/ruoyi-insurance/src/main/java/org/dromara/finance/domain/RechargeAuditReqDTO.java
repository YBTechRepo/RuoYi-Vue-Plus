package org.dromara.finance.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RechargeAuditReqDTO {
    /**
     * 充值记录 ID (必传)
     */
    @NotNull(message = "充值记录ID不能为空")
    private Long id;

    /**
     * 审核状态 (必传): 1-通过，2-驳回
     */
    @NotNull(message = "请选择审核结果")
    private Integer auditStatus;

    /**
     * 实际到账金额 (通过时必填，允许财务手工微调)
     */
    private BigDecimal actualAmount;

    /**
     * 审核备注 (通常驳回时必填，让用户知道为什么死翘翘了)
     */
    private String auditRemark;
}
