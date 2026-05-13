package org.dromara.finance.controller;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckLogin;
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
import org.dromara.finance.domain.vo.BizUserAccountVo;
import org.dromara.finance.domain.bo.BizUserAccountBo;
import org.dromara.finance.service.IBizUserAccountService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 账户信息
 *
 * @author li.xiang
 * @date 2026-03-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/finance/userAccount")
public class BizUserAccountController extends BaseController {

    private final IBizUserAccountService bizUserAccountService;

    /**
     * 查询账户信息列表
     */
    @SaCheckPermission("finance:userAccount:list")
    @GetMapping("/list")
    public TableDataInfo<BizUserAccountVo> list(BizUserAccountBo bo, PageQuery pageQuery) {
        return bizUserAccountService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("finance:userAccount:list")
    @GetMapping("/adminPageList")
    public TableDataInfo<BizUserAccountVo> adminPageList(BizUserAccountBo bo, PageQuery pageQuery) {
        return bizUserAccountService.queryAdminPageList(bo, pageQuery);
    }

    @SaCheckPermission("finance:userAccount:list")
    @GetMapping("/adminList")
    public R<List<BizUserAccountVo>> adminList(BizUserAccountBo bo) {
        return R.ok(bizUserAccountService.queryAdminList(bo));
    }

    /**
     * 导出账户信息列表
     */
    @SaCheckPermission("finance:userAccount:export")
    @Log(title = "账户信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BizUserAccountBo bo, HttpServletResponse response) {
        List<BizUserAccountVo> list = bizUserAccountService.queryAdminList(bo);
        ExcelUtil.exportExcel(list, "账户信息", BizUserAccountVo.class, response);
    }

    /**
     * 获取账户信息详细信息
     *
     * @param userId 主键
     */
    //@SaCheckPermission("finance:userAccount:query")
    @SaCheckLogin
    @GetMapping("/{userId}")
    public R<BizUserAccountVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long userId) {
        return R.ok(bizUserAccountService.queryById(userId));
    }

    /**
     * 新增账户信息
     */
    @SaCheckPermission("finance:userAccount:add")
    @Log(title = "账户信息", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BizUserAccountBo bo) {
        return toAjax(bizUserAccountService.insertByBo(bo));
    }

    /**
     * 修改账户信息
     */
    @SaCheckPermission("finance:userAccount:edit")
    @Log(title = "账户信息", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BizUserAccountBo bo) {
        return toAjax(bizUserAccountService.updateByBo(bo));
    }

    /**
     * 删除账户信息
     *
     * @param userIds 主键串
     */
    @SaCheckPermission("finance:userAccount:remove")
    @Log(title = "账户信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] userIds) {
        return toAjax(bizUserAccountService.deleteWithValidByIds(List.of(userIds), true));
    }
}
