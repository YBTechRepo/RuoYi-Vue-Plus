package org.dromara.insurance.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 代投保订单导出视图对象
 *
 * @author li.xiang
 * @date 2026-05-13
 */
@Data
@ExcelIgnoreUnannotated
public class InsuranceProxyOrderExportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "订单号")
    private String orderNo;

    @ExcelProperty(value = "产品编码")
    private String productCode;

    @ExcelProperty(value = "产品名称")
    private String productName;

    @ExcelProperty(value = "业务员姓名")
    private String agentName;

    @ExcelProperty(value = "客户姓名")
    private String customerName;

    @ExcelProperty(value = "客户手机号")
    private String customerMobile;

    @ExcelProperty(value = "起保日期")
    private Date policyStartDate;

    @ExcelProperty(value = "保单保费")
    private BigDecimal premium;

    @ExcelProperty(value = "订单状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_apply_status")
    private Integer status;

    @ExcelProperty(value = "创建时间")
    private Date createTime;

    @ExcelProperty(value = "净费出单保费")
    private BigDecimal netPremium;

    @ExcelProperty(value = "投保模式", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_product_insure_mode")
    private Integer insureMode;

    @ExcelProperty(value = "产品模式", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_product_mode")
    private Integer productMode;

    @ExcelProperty(value = "支付模式", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_product_payment_mode")
    private Integer paymentMode;

    @ExcelProperty(value = "是否批量单：0-普通单 2-批量子单")
    private Integer isBatch;

    @ExcelProperty(value = "所属批次单号")
    private String batchOrderNo;

    @ExcelProperty(value = "投保人姓名")
    private String appName;

    @ExcelProperty(value = "投保人证件类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_id_type")
    private String appCertType;

    @ExcelProperty(value = "投保人证件号")
    private String appCertNo;

    @ExcelProperty(value = "投保人手机号")
    private String appPhone;

    @ExcelProperty(value = "投保人地址")
    private String appAddress;

    @ExcelProperty(value = "被保人关系", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_relationship_to_insured")
    private String relation;

    @ExcelProperty(value = "被保人姓名")
    private String insuredName;

    @ExcelProperty(value = "被保人证件类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_id_type")
    private String insuredCertType;

    @ExcelProperty(value = "被保人证件号")
    private String insuredCertNo;

    @ExcelProperty(value = "被保人手机号")
    private String insuredPhone;

    @ExcelProperty(value = "被保人地址")
    private String insuredAddress;

}
