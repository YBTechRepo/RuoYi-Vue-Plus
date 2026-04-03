package org.dromara.finance.domain;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RechargeApplyReqDTO {
    /**
     * 前端生成的防重单号 (可选，如果前端不传，后端也会自动生成)
     */
    private String rechargeNo;

    /**
     * 申请充值金额 (必填)
     */
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    private BigDecimal applyAmount;

    /**
     * 支付凭证(转账截图)URL (必填)
     */
    @NotBlank(message = "请上传转账凭证")
    private String voucherImg;
}
