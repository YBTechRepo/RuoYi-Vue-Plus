package org.dromara.insurance.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 佣金配置对象 biz_insurance_product_commission
 *
 * @author li.xiang
 * @date 2026-03-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance_product_commission")
public class InsuranceProductCommission extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 基础佣金比例
     */
    private BigDecimal commissionRate;

    /**
     * 费率生效时间
     */
    private Date effectiveTime;

    /**
     * 费率失效时间
     */
    private Date expirationTime;

    /**
     * 启用状态
     */
    private Integer status;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 删除标志
     */
    @TableLogic
    private String delFlag;


}
