package org.dromara.system.controller.system;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.system.domain.vo.SysUserInviteVo;
import org.dromara.system.domain.bo.SysUserInviteBo;
import org.dromara.system.service.ISysUserInviteService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 人员邀请登记
 *
 * @author li.xiang
 * @date 2026-03-09
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/userInvite")
public class SysUserInviteController extends BaseController {

    private final ISysUserInviteService sysUserInviteService;

    /**
     * 查询人员邀请登记列表
     */
    @SaCheckPermission("system:userInvite:list")
    @GetMapping("/list")
    public TableDataInfo<SysUserInviteVo> list(SysUserInviteBo bo, PageQuery pageQuery) {
        return sysUserInviteService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出人员邀请登记列表
     */
    @SaCheckPermission("system:userInvite:export")
    @Log(title = "人员邀请登记", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SysUserInviteBo bo, HttpServletResponse response) {
        List<SysUserInviteVo> list = sysUserInviteService.queryList(bo);
        ExcelUtil.exportExcel(list, "人员邀请登记", SysUserInviteVo.class, response);
    }

    /**
     * 获取人员邀请登记详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:userInvite:query")
    @GetMapping("/{id}")
    public R<SysUserInviteVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(sysUserInviteService.queryById(id));
    }

    /**
     * 新增人员邀请登记
     */
    @SaCheckPermission("system:userInvite:add")
    @Log(title = "人员邀请登记", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysUserInviteBo bo) {
        return toAjax(sysUserInviteService.insertByBo(bo));
    }

    /**
     * 修改人员邀请登记
     */
    @SaCheckPermission("system:userInvite:edit")
    @Log(title = "人员邀请登记", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SysUserInviteBo bo) {
        return toAjax(sysUserInviteService.updateByBo(bo));
    }

    /**
     * 删除人员邀请登记
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:userInvite:remove")
    @Log(title = "人员邀请登记", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(sysUserInviteService.deleteWithValidByIds(List.of(ids), true));
    }
}
