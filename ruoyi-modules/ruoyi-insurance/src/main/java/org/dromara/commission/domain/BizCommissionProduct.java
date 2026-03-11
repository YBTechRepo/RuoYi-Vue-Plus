package org.dromara.commission.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 特殊产品费率配置对象 biz_commission_product
 *
 * @author li.xiang
 * @date 2026-03-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_commission_product")
public class BizCommissionProduct extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
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
     * 项目负责人比例
     */
    private BigDecimal projectRatio;

    /**
     * 团队负责人比例
     */
    private BigDecimal teamRatio;

    /**
     * 业务员比例
     */
    private BigDecimal salesRatio;

    /**
     * 规则策略
     */
    private Integer ruleStrategy;

    /**
     * 生效开始时间
     */
    private Date effectiveStart;

    /**
     * 生效结束时间
     */
    private Date effectiveEnd;

    /**
     * 状态
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
