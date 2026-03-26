package org.dromara.insurance.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 保险产品-保障责任对象 biz_insurance_product_liability
 *
 * @author li.xiang
 * @date 2026-03-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance_product_liability")
public class InsuranceProductLiability extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 产品ID (关联 biz_insurance_product.id)
     */
    private Long productId;

    /**
     * 责任名称 (如：意外身故/伤残)
     */
    private String liabilityName;

    /**
     * 保障额度说明 (如：50万、按比例赔付)
     */
    private String insuredAmountDesc;

    /**
     * 详情描述 (选填的补充说明)
     */
    private String description;

    /**
     * 排序号 (升序)
     */
    private Long sort;

    /**
     * 乐观锁版本
     */
    @Version
    private Long version;

    /**
     * 删除标记(0-未删除 1-删除)
     */
    @TableLogic
    private String delFlag;


}
