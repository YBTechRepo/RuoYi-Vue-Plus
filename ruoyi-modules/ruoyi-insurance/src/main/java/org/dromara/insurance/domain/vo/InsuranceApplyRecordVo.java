package org.dromara.insurance.domain.vo;

import java.math.BigDecimal;
import org.dromara.insurance.domain.InsuranceApplyRecord;
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
 * 投保记录视图对象 biz_insurance_apply_record
 *
 * @author li.xiang
 * @date 2026-03-13
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = InsuranceApplyRecord.class)
public class InsuranceApplyRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 订单号
     */
    @ExcelProperty(value = "订单号")
    private String orderNo;

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
     * 业务员姓名
     */
    @ExcelProperty(value = "业务员姓名")
    private String agentName;

    /**
     * 业务员ID
     */
    @ExcelProperty(value = "业务员ID")
    private Long agentUserId;

    /**
     * 所属机构ID
     */
    @ExcelProperty(value = "所属机构ID")
    private Long agentDeptId;

    /**
     * 客户姓名
     */
    @ExcelProperty(value = "客户姓名")
    private String customerName;

    /**
     * 客户手机号
     */
    @ExcelProperty(value = "客户手机号")
    private String customerMobile;

    /**
     * 登记保费
     */
    @ExcelProperty(value = "登记保费")
    private BigDecimal premium;

    /**
     * 订单状态
     */
    @ExcelProperty(value = "订单状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_apply_status")
    private Integer status;

    /**
     * 结算状态
     */
    @ExcelProperty(value = "结算状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_commission_status")
    private Integer commissionStatus;

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
     * 净费出单保费
     */
    @ExcelProperty(value = "净费出单保费")
    private BigDecimal netPremium;

    /**
     * 投保模式 0-自投保 1-代投保
     */
    @ExcelProperty(value = "投保模式", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_product_insure_mode")
    private Integer insureMode;

    /**
     * 支付模式 0-常规支付 1-余额代扣
     */
    @ExcelProperty(value = "支付模式", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_product_payment_mode")
    private Integer paymentMode;

}
