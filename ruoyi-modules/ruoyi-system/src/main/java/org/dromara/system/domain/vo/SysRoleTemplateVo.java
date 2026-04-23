package org.dromara.system.domain.vo;

import org.dromara.system.domain.SysRoleTemplate;
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
 * 租户角色管理视图对象 sys_role_template
 *
 * @author li.xiang
 * @date 2026-04-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysRoleTemplate.class)
public class SysRoleTemplateVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    @ExcelProperty(value = "模板ID")
    private Long id;

    /**
     * 模板名称
     */
    @ExcelProperty(value = "模板名称")
    private String templateName;

    /**
     * 模板描述
     */
    @ExcelProperty(value = "模板描述")
    private String remark;

    /**
     * 角色及权限配置(JSON格式)
     */
    @ExcelProperty(value = "角色及权限配置(JSON格式)")
    private String rolesJson;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态")
    private String status;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    @ExcelProperty(value = "租户套餐ID")
    private Long tenantPackageId;
}
