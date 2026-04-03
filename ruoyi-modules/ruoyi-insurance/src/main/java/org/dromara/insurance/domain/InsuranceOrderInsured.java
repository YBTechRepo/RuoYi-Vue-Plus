package org.dromara.insurance.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 被保人明细对象 biz_insurance_order_insured
 *
 * @author li.xiang
 * @date 2026-03-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance_order_insured")
public class InsuranceOrderInsured extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联主订单号
     */
    private String orderNo;

    /**
     * 与投保人关系(0-本人,1-配偶等)
     */
    private String relation;

    /**
     * 被保人姓名
     */
    private String insuredName;

    /**
     * 证件生效起期
     */
    private String certStartDate;

    /**
     * 证件生效止期
     */
    private String certEndDate;

    /**
     * 被保人身份证号
     */
    private String insuredCertNo;

    /**
     * 被保人证件类型(同上)
     */
    private String insuredCertType;

    /**
     * 被保人手机号
     */
    private String insuredPhone;

    /**
     * 被保人地址
     */
    private String insuredAddress;

    /**
     * 
     */
    @Version
    private Integer version;

    /**
     * 
     */
    @TableLogic
    private String delFlag;


}
