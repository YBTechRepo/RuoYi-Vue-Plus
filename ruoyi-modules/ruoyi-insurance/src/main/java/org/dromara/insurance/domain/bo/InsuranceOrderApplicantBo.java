package org.dromara.insurance.domain.bo;

import org.dromara.insurance.domain.InsuranceOrderApplicant;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 投保人信息业务对象 biz_insurance_order_applicant
 *
 * @author li.xiang
 * @date 2026-03-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InsuranceOrderApplicant.class, reverseConvertGenerate = false)
public class InsuranceOrderApplicantBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联主订单号
     */
    private String orderNo;

    /**
     * 投保人姓名
     */
    private String applicantName;

    /**
     * 证件生效起期
     */
    private String certStartDate;

    /**
     * 证件生效止期(长期为9999-12-31)
     */
    private String certEndDate;

    /**
     * 投保人身份证号
     */
    private String applicantCertNo;

    /**
     * 投保人证件类型(字典:0-身份证,1-护照,2-出生证等)
     */
    private String applicantCertType;

    /**
     * 投保人手机号
     */
    private String applicantPhone;

    /**
     * 投保人地址
     */
    private String applicantAddress;

    /**
     * 
     */
    private Integer version;


}
