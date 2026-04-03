package org.dromara.finance.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 账户明细对象 biz_account_flow
 *
 * @author li.xiang
 * @date 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_account_flow")
public class BizAccountFlow extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 资金归属的用户ID
     */
    private Long userId;

    private String userName;

    private String userNickName;

    /**
     * 流水类型
     */
    private Integer flowType;

    /**
     * 变动金额
     */
    private BigDecimal amount;

    private BigDecimal balanceBefore;

    /**
     * 变动后的账户总余额
     */
    private BigDecimal balanceAfter;

    /**
     * 关联业务单号
     */
    private String bizNo;

    /**
     * 流水摘要说明
     */
    private String remark;

    private Date createTime;
}
