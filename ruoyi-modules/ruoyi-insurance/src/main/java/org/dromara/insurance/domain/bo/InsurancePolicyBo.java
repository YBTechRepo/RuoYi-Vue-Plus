package org.dromara.insurance.domain.bo;

import org.dromara.insurance.domain.InsurancePolicy;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 承保保单业务对象 biz_insurance_policy
 *
 * @author li.xiang
 * @date 2026-03-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InsurancePolicy.class, reverseConvertGenerate = false)
public class InsurancePolicyBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品名称
     */
    @NotBlank(message = "产品名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String productName;

    /**
     * 保单号
     */
    @NotBlank(message = "保单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String policyNo;

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderNo;

    /**
     * 业务人员姓名
     */
    @NotBlank(message = "业务人员姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String agentName;

    /**
     * 业务人员id
     */
    @NotNull(message = "业务人员id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long agentUserId;

    /**
     * 业务人员所属部门
     */
    @NotNull(message = "业务人员所属部门不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long agentDeptId;

    /**
     * 保费
     */
    @NotNull(message = "保费不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal premium;

    /**
     * 保额
     */
    @NotNull(message = "保额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal amt;

    /**
     * 结算状态
     */
    @NotNull(message = "结算状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer commissionStatus;

    /**
     * 保单状态
     */
    @NotNull(message = "保单状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer status;

    /**
     * 投保时间
     */
    @NotNull(message = "投保时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date appntDate;

    /**
     * 承保时间
     */
    @NotNull(message = "承保时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date accecptDate;

    /**
     * 保单失效时间
     */
    private Date policyInvalidDate;

    /**
     * 保障开始时间
     */
    @NotNull(message = "保障开始时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date policyStartDate;

    /**
     * 保障结束时间
     */
    @NotNull(message = "保障结束时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date policyEndDate;

    /**
     * 投保人姓名
     */
    @NotBlank(message = "投保人姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String applicantName;

    /**
     * 投保人性别
     */
    @NotBlank(message = "投保人性别不能为空", groups = { AddGroup.class, EditGroup.class })
    private String applicantSex;

    /**
     * 投保人证件号
     */
    @NotBlank(message = "投保人证件号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String applicantIdNo;

    /**
     * 投保人证件类型
     */
    @NotBlank(message = "投保人证件类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String applicantIdType;

    /**
     * 投保人手机号
     */
    @NotBlank(message = "投保人手机号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String applicantMobile;

    /**
     * 被保人与投保人关系
     */
    @NotBlank(message = "被保人与投保人关系不能为空", groups = { AddGroup.class, EditGroup.class })
    private String relationshipToInsured;

    /**
     * 被保人姓名
     */
    @NotBlank(message = "被保人姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String insuredName;

    /**
     * 被保人性别
     */
    @NotBlank(message = "被保人性别不能为空", groups = { AddGroup.class, EditGroup.class })
    private String insuredSex;

    /**
     * 被保人证件号
     */
    @NotBlank(message = "被保人证件号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String insuredIdNo;

    /**
     * 被保人证件类型
     */
    @NotBlank(message = "被保人证件类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String insuredIdType;

    /**
     * 被保人手机号
     */
    @NotBlank(message = "被保人手机号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String insuredMobile;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 乐观锁版本
     */
    private Integer version;

    private String tenantId;

}
