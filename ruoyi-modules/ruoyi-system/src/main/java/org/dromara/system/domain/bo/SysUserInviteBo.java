package org.dromara.system.domain.bo;

import org.dromara.system.domain.SysUserInvite;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 人员邀请登记业务对象 sys_user_invite
 *
 * @author li.xiang
 * @date 2026-03-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysUserInvite.class, reverseConvertGenerate = false)
public class SysUserInviteBo extends BaseEntity {

    /**
     * ID
     */
    @NotNull(message = "ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 用户账号
     */
    //@NotBlank(message = "用户账号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String userName;

    /**
     * 用户姓名
     */
    @NotBlank(message = "用户姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String nickName;

    /**
     * 手机号码
     */
    @NotBlank(message = "手机号码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String phoneNumber;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String idCard;

    /**
     * 推荐人
     */
    @NotBlank(message = "推荐人不能为空", groups = { AddGroup.class, EditGroup.class })
    private String referrerName;

    /**
     * 推荐人id
     */
    @NotNull(message = "推荐人id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long referrerId;

    /**
     * 申请部门ID(可选)
     */
    //@NotNull(message = "申请部门ID(可选)不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long deptId;

    /**
     * 审核状态（0待审核 1已通过 2已拒绝）
     */
    @NotNull(message = "审核状态（0待审核 1已通过 2已拒绝）不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long status;

    /**
     * 审核意见
     */
    private String remark;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 乐观锁版本
     */
    private Long version;

    /**
     * 注册身份
     */
    private String inviteType;

    private Long inviteId;
}
