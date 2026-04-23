package org.dromara.system.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 租户角色管理对象 sys_role_template
 *
 * @author li.xiang
 * @date 2026-04-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role_template")
public class SysRoleTemplate extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板描述
     */
    private String remark;

    /**
     * 角色及权限配置(JSON格式)
     */
    private String rolesJson;

    /**
     * 状态
     */
    private String status;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;

    private Long tenantPackageId;
}
