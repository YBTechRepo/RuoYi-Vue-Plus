package org.dromara.system.domain.vo;

import org.dromara.system.domain.SysUserInvite;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 人员邀请登记视图对象 sys_user_invite
 *
 * @author li.xiang
 * @date 2026-03-09
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysUserInvite.class)
public class SysUserInviteVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @ExcelProperty(value = "ID")
    private Long id;

    /**
     * 用户账号
     */
    @ExcelProperty(value = "用户账号")
    private String userName;

    /**
     * 用户姓名
     */
    @ExcelProperty(value = "用户姓名")
    private String nickName;

    /**
     * 手机号码
     */
    @ExcelProperty(value = "手机号码")
    private String phoneNumber;

    /**
     * 身份证号
     */
    @ExcelProperty(value = "身份证号")
    private String idCard;

    /**
     * 推荐人
     */
    @ExcelProperty(value = "推荐人")
    private String referrerName;

    /**
     * 推荐人id
     */
    @ExcelProperty(value = "推荐人id")
    private Long referrerId;

    /**
     * 申请部门ID(可选)
     */
    @ExcelProperty(value = "申请部门ID(可选)")
    private Long deptId;

    /**
     * 审核状态（0待审核 1已通过 2已拒绝）
     */
    @ExcelProperty(value = "审核状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "user_invite_audit_status")
    private Long status;

    /**
     * 审核意见
     */
    @ExcelProperty(value = "审核意见")
    private String remark;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 删除标识
     */
    @ExcelProperty(value = "删除标识")
    private String delFlag;

    /**
     * 乐观锁版本
     */
    @ExcelProperty(value = "乐观锁版本")
    private Long version;

    @ExcelProperty(value = "注册身份")
    private String inviteType;

}
