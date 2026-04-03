package org.dromara.finance.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 账户信息对象 biz_user_account
 *
 * @author li.xiang
 * @date 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_user_account")
public class BizUserAccount extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "user_id")
    private Long userId;

    /**
     * 用户编号
     */
    private String userName;

    /**
     * 用户姓名
     */
    private String userNickName;

    /**
     * 可用充值余额
     */
    private BigDecimal balance;

    /**
     * 账户状态
     */
    private Integer status;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;


}
