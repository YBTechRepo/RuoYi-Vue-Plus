package org.dromara.insurance.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 服务费配置对象
 */
@Data
public class ServiceFeeConfig {

    /**
     * 服务费比例 (例如 0.26)
     */
    private BigDecimal feeRatio;

    /**
     * 有效开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveStartTime;

    /**
     * 有效结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveEndTime;

}
