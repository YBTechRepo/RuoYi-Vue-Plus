package org.dromara.insurance.domain.vo;

import java.math.BigDecimal;

import org.dromara.insurance.domain.InsuranceTenantProduct;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 产品库视图对象 biz_insurance_tenant_product
 *
 * @author li.xiang
 * @date 2026-03-20
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = InsuranceTenantProduct.class)
public class InsuranceTenantProductVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    //@ExcelProperty(value = "ID")
    private Long id;

    /**
     * 产品ID
     */
    @ExcelProperty(value = "产品ID")
    private Long productId;

    /**
     * 保险公司
     */
    @ExcelProperty(value = "保险公司")
    private String companyCode;

    /**
     * 产品代码
     */
    @ExcelProperty(value = "产品代码")
    private String productCode;

    /**
     * 产品名称
     */
    @ExcelProperty(value = "产品名称")
    private String productName;

    /**
     * 产品类垈
     */
    @ExcelProperty(value = "产品类垈", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_product_type")
    private String productType;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 所属分类名称
     */
    @ExcelProperty(value = "所属分类名称")
    private String categoryName;

    /**
     * 营销标签
     */
    @ExcelProperty(value = "营销标签")
    private String marketingTags;

    /**
     * 产品模式
     */
    @ExcelProperty(value = "产品模式", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_product_mode")
    private Integer productMode;

    /**
     * 投保模式
     */
    private Integer insureMode;

    /**
     * 支付模式
     */
    private Integer paymentMode;

    /**
     * 最低保费
     */
    @ExcelProperty(value = "最低保费")
    private BigDecimal minPremium;

    /**
     * 投保链接
     */
    private String proposalUrl;


    private String imgUrl;

    /**
     * 产品特点
     */
    @ExcelProperty(value = "产品特点")
    private String description;

    /**
     * 上架状态
     */
    @ExcelProperty(value = "上架状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_product_status")
    private String status;

    /**
     * 自定义排埏
     */
    @ExcelProperty(value = "自定义排埏")
    private Integer sort;

    /**
     * 乐观锁版本
     */
    //@ExcelProperty(value = "乐观锅版本")
    private Integer version;

    /**
     * 刀陦懇记
     */
    //@ExcelProperty(value = "刀陦懇记")
    private String delFlag;

    /**
     * 服务费 (费率)
     */
    private BigDecimal serviceFee;

    /**
     * 净费燺单保费
     */
    private BigDecimal netPremium;

    /**
     * 当前登录用户按角色可展示的佣金费率
     */
    private BigDecimal displayCommissionRate;

}
