package org.dromara.insurance.domain.vo;

import org.dromara.insurance.domain.InsuranceOrderApplicant;
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
 * 投保人信息视图对象 biz_insurance_order_applicant
 *
 * @author li.xiang
 * @date 2026-03-30
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = InsuranceOrderApplicant.class)
public class InsuranceOrderApplicantVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 关联主订单号
     */
    @ExcelProperty(value = "关联主订单号")
    private String orderNo;

    /**
     * 投保人姓名
     */
    @ExcelProperty(value = "投保人姓名")
    private String applicantName;

    /**
     * 证件生效起期
     */
    @ExcelProperty(value = "证件生效起期")
    private String certStartDate;

    /**
     * 证件生效止期(长期为9999-12-31)
     */
    @ExcelProperty(value = "证件生效止期(长期为9999-12-31)")
    private String certEndDate;

    /**
     * 投保人身份证号
     */
    @ExcelProperty(value = "投保人身份证号")
    private String applicantCertNo;

    /**
     * 投保人证件类型(字典:0-身份证,1-护照,2-出生证等)
     */
    @ExcelProperty(value = "投保人证件类型(字典:0-身份证,1-护照,2-出生证等)", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_id_type")
    private String applicantCertType;

    /**
     * 投保人手机号
     */
    @ExcelProperty(value = "投保人手机号")
    private String applicantPhone;

    /**
     * 投保人地址
     */
    @ExcelProperty(value = "投保人地址")
    private String applicantAddress;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Integer version;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private String tenantId;


}
