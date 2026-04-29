package org.dromara.insurance.domain.bo;

import org.dromara.insurance.domain.InsuranceProductCategory;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品分类管理业务对象 biz_insurance_product_category
 *
 * @author lixiang
 * @date 2026-04-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InsuranceProductCategory.class, reverseConvertGenerate = false)
public class InsuranceProductCategoryBo extends BaseEntity {

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 祖级列表
     */
    private String ancestors;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 绑定的营销标签(逗号分隔)
     */
    private String marketingTags;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 显示顺序
     */
    private Long sort;

    /**
     * 分类状态（0正常 1停用）
     */
    private Long status;

    /**
     * 删除标志
     */
    private String delFlag;

    /**
     *
     */
    private Long version;

    private String iconColor;

}
