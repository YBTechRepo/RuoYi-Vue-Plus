package org.dromara.system.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 人员邀请登记对象 sys_user_invite
 *
 * @author li.xiang
 * @date 2026-03-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_invite")
public class SysUserInvite extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户姓名
     */
    private String nickName;

    /**
     * 手机号码
     */
    private String phoneNumber;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 推荐人
     */
    private String referrerName;

    /**
     * 推荐人id
     */
    private Long referrerId;

    /**
     * 申请部门ID(可选)
     */
    private Long deptId;

    /**
     * 审核状态（0待审核 1已通过 2已拒绝）
     */
    private Long status;

    /**
     * 审核意见
     */
    private String remark;

    /**
     * 删除标识
     */
    @TableLogic
    private String delFlag;

    /**
     * 乐观锁版本
     */
    @Version
    private Long version;


}
