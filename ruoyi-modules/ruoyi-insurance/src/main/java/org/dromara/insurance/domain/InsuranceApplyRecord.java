package org.dromara.insurance.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

import java.io.Serial;
import java.util.Date;

/**
 * 投保记录对象 biz_insurance_apply_record
 *
 * @author li.xiang
 * @date 2026-03-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance_apply_record")
public class InsuranceApplyRecord extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 产品ID
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
     * 业务员姓名
     */
    private String agentName;

    /**
     * 业务员ID
     */
    private Long agentUserId;

    /**
     * 所属机构ID
     */
    private Long agentDeptId;

    /**
     * 客户姓名
     */
    private String customerName;

    /**
     * 客户手机号
     */
    private String customerMobile;

    /**
     * 登记保费
     */
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
    @TableLogic
    private String delFlag;

    /**
     * 乐观锁版本
     */
    @Version
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

    private Date payTime;

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
