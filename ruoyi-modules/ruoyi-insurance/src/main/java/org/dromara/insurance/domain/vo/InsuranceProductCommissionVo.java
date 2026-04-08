package org.dromara.insurance.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.insurance.domain.InsuranceProductCommission;
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
 * 佣金配置视图对象 biz_insurance_product_commission
 *
 * @author li.xiang
 * @date 2026-03-06
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = InsuranceProductCommission.class)
public class InsuranceProductCommissionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    //@ExcelProperty(value = "id")
    private Long id;

    /**
     * 产品ID
     */
    @ExcelProperty(value = "产品ID")
    private Long productId;

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
     * 基础佣金比例
     */
    @ExcelProperty(value = "基础佣金比例")
    private BigDecimal commissionRate;

    /**
     * 费率生效时间
     */
    @ExcelProperty(value = "费率生效时间")
    private Date effectiveTime;

    /**
     * 费率失效时间
     */
    @ExcelProperty(value = "费率失效时间")
    private Date expirationTime;

    /**
     * 启用状态
     */
    @ExcelProperty(value = "启用状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_product_commission_status")
    private Integer status;

    /**
     * 乐观锁版本号
     */
    //@ExcelProperty(value = "乐观锁版本号")
    private Integer version;

    /**
     * 删除标志
     */
    //@ExcelProperty(value = "删除标志")
    private String delFlag;

    /**
     * 佣金配置 (JSON数组)
     */
    private String commissionConfig;
}
