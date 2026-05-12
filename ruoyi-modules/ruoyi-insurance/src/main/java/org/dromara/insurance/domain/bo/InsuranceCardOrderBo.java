package org.dromara.insurance.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.insurance.domain.InsuranceCardOrder;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 卡密订单业务对象 biz_insurance_card_order
 *
 * @author li.xiang
 * @date 2026-05-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InsuranceCardOrder.class, reverseConvertGenerate = false)
public class InsuranceCardOrderBo extends BaseEntity {

    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    private String tenantId;

    @NotBlank(message = "订单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderNo;

    @NotNull(message = "产品ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long productId;

    @NotBlank(message = "产品编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String productCode;

    @NotBlank(message = "产品名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String productName;

    private String agentName;

    @NotNull(message = "业务员ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long agentUserId;

    private Long agentDeptId;

    @NotBlank(message = "客户姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String customerName;

    @NotBlank(message = "客户手机号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String customerMobile;

    private Integer status;
    private Integer productMode;
    private Integer insureMode;
    private Integer paymentMode;
    private Date payTime;
    private String specId;
    private String specName;
    private Integer goodsQuantity;
    private BigDecimal goodsAmount;
    private BigDecimal freightAmount;
    private String freightPayType;
    private BigDecimal payAmount;
    private String selectedCompanyCode;
    private String receiverName;
    private String receiverMobile;
    private String receiverAddress;
    private String expressCompany;
    private String expressNo;
    private Date deliveryTime;
    private String delFlag;
    private Integer version;
}


