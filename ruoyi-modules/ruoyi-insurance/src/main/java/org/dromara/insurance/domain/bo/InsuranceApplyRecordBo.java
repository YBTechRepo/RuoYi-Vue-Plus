package org.dromara.insurance.domain.bo;

import org.dromara.insurance.domain.InsuranceApplyRecord;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 投保记录业务对象 biz_insurance_apply_record
 *
 * @author li.xiang
 * @date 2026-03-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InsuranceApplyRecord.class, reverseConvertGenerate = false)
public class InsuranceApplyRecordBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderNo;

    /**
     * 产品ID
     */
    @NotNull(message = "产品ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long productId;

    /**
     * 产品编码
     */
    @NotBlank(message = "产品编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String productCode;

    /**
     * 产品名称
     */
    @NotBlank(message = "产品名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String productName;

    /**
     * 业务员姓名
     */
    @NotBlank(message = "业务员姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String agentName;

    /**
     * 业务员ID
     */
    @NotNull(message = "业务员ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long agentUserId;

    /**
     * 所属机构ID
     */
    @NotNull(message = "所属机构ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long agentDeptId;

    /**
     * 客户姓名
     */
    @NotBlank(message = "客户姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String customerName;

    /**
     * 客户手机号
     */
    @NotBlank(message = "客户手机号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String customerMobile;

    /**
     * 登记保费
     */
    @NotNull(message = "登记保费不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal premium;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 结算状态
     */
    private Integer commissionStatus;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 乐观锁版本
     */
    private Integer version;

    /**
     * 净费出单保费
     */
    private BigDecimal netPremium;

    /**
     * 投保模式 0-自投保 1-代投保
     */
    private Integer insureMode;

    /**
     * 产品模式
     */
    private Integer productMode;

    /**
     * 支付模式 0-常规支付 1-余额代扣
     */
    private Integer paymentMode;

    /**
     * 是否批量单：0-普通单 1-批次主单 2-批量子单
     */
    private Integer isBatch;

    /**
     * 所属批次单号（子单指向主单）
     */
    private String batchOrderNo;

    /**
     * 卡密规格ID
     */
    private String specId;

    /**
     * 卡密规格名称
     */
    private String specName;

    /**
     * 商品金额
     */
    private BigDecimal goodsAmount;

    /**
     * 运费金额
     */
    private BigDecimal freightAmount;

    /**
     * 购买时选择的保险公司
     */
    private String selectedCompanyCode;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人手机号
     */
    private String receiverMobile;

    /**
     * 收货地址
     */
    private String receiverAddress;

    /**
     * 投保扩展字段值 (JSON对象)
     */
    private String insureExtraData;
}
