package org.dromara.insurance.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 产品佣金配置项
 */
@Data
public class ProductCommissionConfig {

    /**
     * 佣金比例 (例如 0.3000)
     */
    private BigDecimal commissionRate;

    /**
     * 生效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveTime;

    /**
     * 失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expirationTime;

}
