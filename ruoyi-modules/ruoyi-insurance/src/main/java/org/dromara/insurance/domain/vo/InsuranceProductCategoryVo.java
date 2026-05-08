package org.dromara.insurance.domain.vo;

import org.dromara.insurance.domain.InsuranceProductCategory;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 产品分类管理视图对象 biz_insurance_product_category
 *
 * @author lixiang
 * @date 2026-04-29
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = InsuranceProductCategory.class)
public class InsuranceProductCategoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    @ExcelProperty(value = "分类ID")
    private Long categoryId;

    /**
     * 父分类ID
     */
    @ExcelProperty(value = "父分类ID")
    private Long parentId;

    /**
     * 祖级列表
     */
    @ExcelProperty(value = "祖级列表")
    private String ancestors;

    /**
     * 分类名称
     */
    @ExcelProperty(value = "分类名称")
    private String categoryName;

    /**
     * 绑定的营销标签(逗号分隔)
     */
    @ExcelProperty(value = "绑定的营销标签")
    private String marketingTags;

    /**
     * 分类图标
     */
    @ExcelProperty(value = "分类图标 ")
    private String icon;

    /**
     * 显示顺序
     */
    @ExcelProperty(value = "显示顺序")
    private Long sort;

    /**
     * 分类状态（0正常 1停用）
     */
    @ExcelProperty(value = "分类状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=正常,1=停用")
    private Long status;

    /**
     * 删除标志
     */
    private String delFlag;


    private Long version;

    private String iconColor;
}
