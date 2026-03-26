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
    @ExcelProperty(value = "ID")
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
     * 产品类型
     */
    @ExcelProperty(value = "产品类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_product_type")
    private String productType;

    /**
     * 产品模式
     */
    @ExcelProperty(value = "产品模式", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_product_mode")
    private Integer productMode;

    /**
     * 最低保费
     */
    @ExcelProperty(value = "最低保费")
    private BigDecimal minPremium;


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
     * 自定义排序
     */
    @ExcelProperty(value = "自定义排序")
    private Integer sort;

    /**
     * 乐观锁版本
     */
    @ExcelProperty(value = "乐观锁版本")
    private Integer version;

    /**
     * 删除标记
     */
    @ExcelProperty(value = "删除标记")
    private String delFlag;

    /**
     * 服务费 (费率)
     */
    private BigDecimal serviceFee;

    /**
     * 净费出单保费
     */
    private BigDecimal netPremium;

}
