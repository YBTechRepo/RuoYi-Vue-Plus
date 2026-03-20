package org.dromara.insurance.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

import java.io.Serial;

/**
 * 产品库对象 biz_insurance_tenant_product
 *
 * @author li.xiang
 * @date 2026-03-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance_tenant_product")
public class InsuranceTenantProduct extends TenantEntity {

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
     * 上架状态
     */
    private String status;

    /**
     * 自定义排序
     */
    private Integer sort;

    /**
     * 乐观锁版本
     */
    @Version
    private Integer version;

    /**
     * 删除标记
     */
    @TableLogic
    private String delFlag;


}
