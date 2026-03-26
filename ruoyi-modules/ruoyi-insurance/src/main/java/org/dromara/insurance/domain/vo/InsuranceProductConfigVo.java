package org.dromara.insurance.domain.vo;

import java.math.BigDecimal;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.insurance.domain.InsuranceProductConfig;
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
 * @date 2026-03-06
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = InsuranceProductConfig.class)
public class InsuranceProductConfigVo implements Serializable {

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
     * 保险公司
     */
    @ExcelProperty(value = "保险公司", converter = ExcelDictConvert.class)
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
    private Integer productMode;

    /**
     * 最低保费
     */
    @ExcelProperty(value = "最低保费")
    private BigDecimal minPremium;

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
    private Integer status;

    /**
     * 产品排序
     */
    @ExcelProperty(value = "产品排序")
    private Integer sort;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 删除标识
     */
    @ExcelProperty(value = "删除标识")
    private String delFlag;

    /**
     * 乐观锁版本
     */
    @ExcelProperty(value = "乐观锁版本")
    private Integer version;

    /**
     * 产品特点 (逗号拼接)
     */
    @ExcelProperty(value = "产品特点")
    private String productFeatures;

    /**
     * 服务费配置 (JSON数组)
     */
    private String serviceFeeConfig;
}
