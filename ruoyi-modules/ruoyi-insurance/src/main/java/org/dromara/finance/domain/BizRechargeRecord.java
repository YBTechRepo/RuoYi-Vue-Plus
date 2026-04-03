package org.dromara.finance.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.translation.annotation.Translation;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;

/**
 * 充值申请对象 biz_recharge_record
 *
 * @author li.xiang
 * @date 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_recharge_record")
public class BizRechargeRecord extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 充值单号
     */
    private String rechargeNo;

    /**
     * 申请人ID
     */
    private Long userId;

    /**
     * 申请人账号
     */
    private String userName;

    /**
     * 申请人姓名
     */
    private String userNickName;

    /**
     * 用户填写的申请充值金额
     */
    private BigDecimal applyAmount;

    /**
     * 财务实际核准到账的金额
     */
    private BigDecimal actualAmount;

    /**
     * 支付凭证(转账截图)URL
     */
    private String voucherImg;

    /**
     * 审核状态: 0=待审核, 1=已通过, 2=已驳回
     */
    private Integer status;

    /**
     * 审核/驳回备注说明
     */
    private String auditRemark;

    /**
     * 审核人ID
     */
    private Long auditBy;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    private Date createTime;

}
