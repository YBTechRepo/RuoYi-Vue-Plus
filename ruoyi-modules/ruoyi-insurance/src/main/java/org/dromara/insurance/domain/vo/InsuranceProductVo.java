package org.dromara.insurance.domain.vo;

import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.insurance.domain.InsuranceProduct;
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
 * 产品配置视图对象 biz_insurance_product
 *
 * @author li.xiang
 * @date 2026-03-02
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = InsuranceProduct.class)
public class InsuranceProductVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 产品编码
     */
    @ExcelProperty(value = "产品编码")
    private String productCode;

    /**
     * 产品名称
     */
    @ExcelProperty(value = "产品名称")
    private String productName;

    /**
     * 保险公司编码
     */
    @ExcelProperty(value = "保险公司编码", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_company")
    private String companyCode;

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
    private Long productMode;

    /**
     * 最低保费
     */
    @ExcelProperty(value = "最低保费")
    private Long minPremium;

    /**
     * 投保链接
     */
    @ExcelProperty(value = "投保链接")
    private String proposalUrl;

    /**
     * 产品图片
     */
    @ExcelProperty(value = "产品图片")
    private String imgUrl;

    /**
     * 产品图片Url
     */
    @Translation(type = TransConstant.OSS_ID_TO_URL, mapper = "imgUrl")
    private String imgUrlUrl;
    /**
     * 产品说明
     */
    @ExcelProperty(value = "产品说明")
    private String description;

    /**
     * 产品状态
     */
    @ExcelProperty(value = "产品状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_product_status")
    private Long status;

    /**
     * 产品排序
     */
    @ExcelProperty(value = "产品排序")
    private Long sort;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updateTime;


}
