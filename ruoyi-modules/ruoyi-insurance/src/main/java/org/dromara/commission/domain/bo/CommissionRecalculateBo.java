package org.dromara.commission.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 佣金重算请求
 *
 * @author li.xiang
 */
@Data
public class CommissionRecalculateBo {

    /**
     * 订单号或保单号
     */
    @NotBlank(message = "订单号或保单号不能为空")
    private String bizNo;
}
