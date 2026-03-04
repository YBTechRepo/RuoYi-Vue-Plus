package org.dromara.insurance.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 承保保单对象 biz_insurance_policy
 *
 * @author li.xiang
 * @date 2026-03-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance_policy")
public class InsurancePolicy extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
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
    private String productName;

    /**
     * 保单号
     */
    private String policyNo;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 业务人员姓名
     */
    private String agentName;

    /**
     * 业务人员id
     */
    private Long agentUserId;

    /**
     * 业务人员所属部门
     */
    private Long agentDeptId;

    /**
     * 保费
     */
    private BigDecimal premium;

    /**
     * 保额
     */
    private BigDecimal amt;

    /**
     * 结算状态
     */
    private Integer commissionStatus;

    /**
     * 保单状态
     */
    private Integer status;

    /**
     * 投保时间
     */
    private Date appntDate;

    /**
     * 承保时间
     */
    private Date accecptDate;

    /**
     * 保单失效时间
     */
    private Date policyInvalidDate;

    /**
     * 保障开始时间
     */
    private Date policyStartDate;

    /**
     * 保障结束时间
     */
    private Date policyEndDate;

    /**
     * 投保人姓名
     */
    private String applicantName;

    /**
     * 投保人性别
     */
    private String applicantSex;

    /**
     * 投保人证件号
     */
    private String applicantIdNo;

    /**
     * 投保人证件类型
     */
    private String applicantIdType;

    /**
     * 投保人手机号
     */
    private String applicantMobile;

    /**
     * 被保人与投保人关系
     */
    private String relationshipToInsured;

    /**
     * 被保人性别
     */
    private String insuredSex;

    /**
     * 被保人证件号
     */
    private String insuredIdNo;

    /**
     * 被保人证件类型
     */
    private String insuredIdType;

    /**
     * 被保人手机号
     */
    private String insuredMobile;

    /**
     * 删除标识
     */
    private Integer deletedFlag;


}
