package org.dromara.insurance.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AutoMapper(target = InsuranceProductConfigVo.class)
public class InsuranceSalesProductVo {

    private Long id;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 保险公司
     */
    @ExcelDictFormat(dictType = "insurance_company")
    private String companyCode;

    /**
     * 产品类型
     */
    @ExcelDictFormat(dictType = "insurance_product_type")
    private String productType;

    /**
     * 产品模式
     */
    @ExcelDictFormat(dictType = "insurance_product_mode")
    private Integer productMode;

    /**
     * 最低保费
     */
    private BigDecimal minPremium;

    /**
     * 投保链接
     */
    private String proposalUrl;

    /**
     * 产品图片
     */
    private String imgUrl;

    /**
     * 产品图片Url
     */

    private String imgUrlUrl;
    /**
     * 产品说明
     */
    private String description;

    /**
     * 产品状态
     */
    @ExcelDictFormat(dictType = "insurance_product_status")
    private Integer status;

    /**
     * 产品排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 乐观锁版本
     */
    private Integer version;

    // 🌟 BFF 聚合新增字段：计算后的最终展示费率
    private BigDecimal displayCommissionRate;

    private Long tenantProductId;

    private String productFeatures;

    private Integer paymentMode;

    private Integer insureMode;
}
