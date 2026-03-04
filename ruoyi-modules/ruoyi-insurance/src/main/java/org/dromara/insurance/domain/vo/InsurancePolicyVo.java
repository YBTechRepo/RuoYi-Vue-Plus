package org.dromara.insurance.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.insurance.domain.InsurancePolicy;
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
 * 承保保单视图对象 biz_insurance_policy
 *
 * @author li.xiang
 * @date 2026-03-02
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = InsurancePolicy.class)
public class InsurancePolicyVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 产品id
     */
    @ExcelProperty(value = "产品id")
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
     * 保单号
     */
    @ExcelProperty(value = "保单号")
    private String policyNo;

    /**
     * 订单号
     */
    @ExcelProperty(value = "订单号")
    private String orderNo;

    /**
     * 业务人员姓名
     */
    @ExcelProperty(value = "业务人员姓名")
    private String agentName;

    /**
     * 业务人员id
     */
    @ExcelProperty(value = "业务人员id")
    private Long agentUserId;

    /**
     * 业务人员所属部门
     */
    @ExcelProperty(value = "业务人员所属部门")
    private Long agentDeptId;

    /**
     * 保费
     */
    @ExcelProperty(value = "保费")
    private BigDecimal premium;

    /**
     * 保额
     */
    @ExcelProperty(value = "保额")
    private BigDecimal amt;

    /**
     * 结算状态
     */
    @ExcelProperty(value = "结算状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_commission_status")
    private Integer commissionStatus;

    /**
     * 保单状态
     */
    @ExcelProperty(value = "保单状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_policy_status")
    private Integer status;

    /**
     * 投保时间
     */
    @ExcelProperty(value = "投保时间")
    private Date appntDate;

    /**
     * 承保时间
     */
    @ExcelProperty(value = "承保时间")
    private Date accecptDate;

    /**
     * 保单失效时间
     */
    @ExcelProperty(value = "保单失效时间")
    private Date policyInvalidDate;

    /**
     * 保障开始时间
     */
    @ExcelProperty(value = "保障开始时间")
    private Date policyStartDate;

    /**
     * 保障结束时间
     */
    @ExcelProperty(value = "保障结束时间")
    private Date policyEndDate;

    /**
     * 投保人姓名
     */
    @ExcelProperty(value = "投保人姓名")
    private String applicantName;

    /**
     * 投保人性别
     */
    @ExcelProperty(value = "投保人性别")
    private String applicantSex;

    /**
     * 投保人证件号
     */
    @ExcelProperty(value = "投保人证件号")
    private String applicantIdNo;

    /**
     * 投保人证件类型
     */
    @ExcelProperty(value = "投保人证件类型")
    private String applicantIdType;

    /**
     * 投保人手机号
     */
    @ExcelProperty(value = "投保人手机号")
    private String applicantMobile;

    /**
     * 被保人与投保人关系
     */
    @ExcelProperty(value = "被保人与投保人关系")
    private String relationshipToInsured;

    /**
     * 被保人性别
     */
    @ExcelProperty(value = "被保人性别")
    private String insuredSex;

    /**
     * 被保人证件号
     */
    @ExcelProperty(value = "被保人证件号")
    private String insuredIdNo;

    /**
     * 被保人证件类型
     */
    @ExcelProperty(value = "被保人证件类型")
    private String insuredIdType;

    /**
     * 被保人手机号
     */
    @ExcelProperty(value = "被保人手机号")
    private String insuredMobile;
}
