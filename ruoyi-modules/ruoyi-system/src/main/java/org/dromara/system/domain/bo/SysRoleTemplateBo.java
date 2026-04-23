package org.dromara.system.domain.bo;

import org.dromara.system.domain.SysRoleTemplate;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 租户角色管理业务对象 sys_role_template
 *
 * @author li.xiang
 * @date 2026-04-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysRoleTemplate.class, reverseConvertGenerate = false)
public class SysRoleTemplateBo extends BaseEntity {

    /**
     * 模板ID
     */
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

    private Long tenantPackageId;
}
