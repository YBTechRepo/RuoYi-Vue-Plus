package org.dromara.insurance.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 产品分类管理对象 biz_insurance_product_category
 *
 * @author lixiang
 * @date 2026-04-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance_product_category")
public class InsuranceProductCategory extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    @TableId(value = "category_id")
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
    @TableLogic
    private String delFlag;

    /**
     *
     */
    @Version
    private Long version;

    /**
     * 绑定的营销标签(逗号分隔)
     */
    private String marketingTags;

    private String iconColor;
}
