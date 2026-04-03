package org.dromara.insurance.domain.bo;

import org.dromara.insurance.domain.InsuranceOrderInsured;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 被保人明细业务对象 biz_insurance_order_insured
 *
 * @author li.xiang
 * @date 2026-03-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InsuranceOrderInsured.class, reverseConvertGenerate = false)
public class InsuranceOrderInsuredBo extends BaseEntity {

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
    private Integer version;


}
