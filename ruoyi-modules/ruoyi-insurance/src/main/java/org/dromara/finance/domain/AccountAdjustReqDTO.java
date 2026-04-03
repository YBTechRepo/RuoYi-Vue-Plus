package org.dromara.finance.domain;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountAdjustReqDTO {
    @NotNull(message = "请选择要调账的用户")
    private Long userId;

    /**
     * 流水类型: 4-其他收入(加钱), 5-其他支出(扣钱)
     */
    @NotNull(message = "请选择流水类型")
    private Integer flowType;

    @NotNull(message = "变动金额不能为空")
    @DecimalMin(value = "0.01", message = "变动金额必须大于0")
    private BigDecimal amount;

    @NotBlank(message = "请填写流水摘要，以便后续对账")
    private String remark;
}
