package org.dromara.insurance.domain.vo;

import org.dromara.insurance.domain.InsuranceOrderInsured;
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
 * 被保人明细视图对象 biz_insurance_order_insured
 *
 * @author li.xiang
 * @date 2026-03-30
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = InsuranceOrderInsured.class)
public class InsuranceOrderInsuredVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    //@ExcelProperty(value = "主键")
    private Long id;

    /**
     * 关联主订单号
     */
    @ExcelProperty(value = "关联主订单号")
    private String orderNo;

    /**
     * 与投保人关系(0-本人,1-配偶等)
     */
    @ExcelProperty(value = "与投保人关系", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_relationship_to_insured")
    private String relation;

    /**
     * 被保人姓名
     */
    @ExcelProperty(value = "被保人姓名")
    private String insuredName;

    /**
     * 证件生效起期
     */
    @ExcelProperty(value = "证件生效起期")
    private String certStartDate;

    /**
     * 证件生效止期
     */
    @ExcelProperty(value = "证件生效止期")
    private String certEndDate;

    /**
     * 被保人身份证号
     */
    @ExcelProperty(value = "被保人身份证号")
    private String insuredCertNo;

    /**
     * 被保人证件类型(同上)
     */
    @ExcelProperty(value = "被保人证件类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "insurance_id_type")
    private String insuredCertType;

    /**
     * 被保人手机号
     */
    @ExcelProperty(value = "被保人手机号")
    private String insuredPhone;

    /**
     * 被保人地址
     */
    @ExcelProperty(value = "被保人地址")
    private String insuredAddress;

    /**
     *
     */
    //@ExcelProperty(value = "")
    private Integer version;

    /**
     *
     */
    //@ExcelProperty(value = "")
    private String tenantId;


}
