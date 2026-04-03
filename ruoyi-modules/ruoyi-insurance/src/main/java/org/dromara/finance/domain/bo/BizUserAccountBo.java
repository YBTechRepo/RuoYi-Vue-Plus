package org.dromara.finance.domain.bo;

import org.dromara.finance.domain.BizUserAccount;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 账户信息业务对象 biz_user_account
 *
 * @author li.xiang
 * @date 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BizUserAccount.class, reverseConvertGenerate = false)
public class BizUserAccountBo extends BaseEntity {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = { EditGroup.class })
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
    private Integer version;


}
