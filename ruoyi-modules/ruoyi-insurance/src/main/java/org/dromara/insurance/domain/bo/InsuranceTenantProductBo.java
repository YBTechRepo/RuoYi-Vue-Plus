package org.dromara.insurance.domain.bo;

import org.dromara.insurance.domain.InsuranceTenantProduct;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 产品库业务对象 biz_insurance_tenant_product
 *
 * @author li.xiang
 * @date 2026-03-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InsuranceTenantProduct.class, reverseConvertGenerate = false)
public class InsuranceTenantProductBo extends BaseEntity {

    /**
     * ID
     */
    private Long id;

    /**
     * 产品ID
     */
    @NotNull(message = "产品ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long productId;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 所属分类名称
     */
    private String categoryName;

    /**
     * 营销标签
     */
    private String marketingTags;

    /**
     * 产品模式（查询条件，来自平台产品表）
     */
    private Integer productMode;

    /**
     * 上架状态
     */
    @NotBlank(message = "上架状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private String status;

    /**
     * 自定义排序
     */
    private Integer sort;

    /**
     * 乐观锁版本
     */
    private Integer version;

    /**
     * 删除标记
     */
    private String delFlag;

}
