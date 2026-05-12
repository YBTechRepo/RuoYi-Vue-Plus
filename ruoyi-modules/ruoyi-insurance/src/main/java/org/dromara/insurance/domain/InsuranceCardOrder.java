package org.dromara.insurance.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 卡密订单对象 biz_insurance_card_order
 *
 * @author li.xiang
 * @date 2026-05-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance_card_order")
public class InsuranceCardOrder extends TenantEntity {

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
     * 订单状态：0-已支付 1-未提交 3-待支付 4-已取消 5-已发货
     */
    private Integer status;

    /**
     * 产品模式
     */
    private Integer productMode;

    /**
     * 投保模式
     */
    private Integer insureMode;

    /**
     * 支付模式
     */
    private Integer paymentMode;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 卡密规格ID
     */
    private String specId;

    /**
     * 卡密规格名称
     */
    private String specName;

    /**
     * 购买数量
     */
    private Integer goodsQuantity;

    /**
     * 商品金额
     */
    private BigDecimal goodsAmount;

    /**
     * 运费金额
     */
    private BigDecimal freightAmount;

    /**
     * 运费支付方式
     */
    private String freightPayType;

    /**
     * 应付金额
     */
    private BigDecimal payAmount;

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
     * 快递公司
     */
    private String expressCompany;

    /**
     * 快递单号
     */
    private String expressNo;

    /**
     * 发货时间
     */
    private Date deliveryTime;

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
}

