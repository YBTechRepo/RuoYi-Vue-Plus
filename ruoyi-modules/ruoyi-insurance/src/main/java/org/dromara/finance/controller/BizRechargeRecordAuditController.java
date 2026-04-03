package org.dromara.finance.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.finance.domain.bo.BizRechargeRecordBo;
import org.dromara.finance.domain.vo.BizRechargeRecordVo;
import org.dromara.finance.service.IBizRechargeRecordService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 充值申请
 *
 * @author li.xiang
 * @date 2026-03-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/finance/rechargeRecordAudit")
public class BizRechargeRecordAuditController extends BaseController {

    private final IBizRechargeRecordService bizRechargeRecordService;

    /**
     * 查询充值申请列表
     */
    @SaCheckPermission("finance:rechargeRecord:list")
    @GetMapping("/list")
    public TableDataInfo<BizRechargeRecordVo> list(BizRechargeRecordBo bo, PageQuery pageQuery) {
        return bizRechargeRecordService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出充值申请列表
     */
    @SaCheckPermission("finance:rechargeRecord:export")
    @Log(title = "充值申请", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BizRechargeRecordBo bo, HttpServletResponse response) {
        List<BizRechargeRecordVo> list = bizRechargeRecordService.queryList(bo);
        ExcelUtil.exportExcel(list, "充值申请", BizRechargeRecordVo.class, response);
    }

    /**
     * 获取充值申请详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("finance:rechargeRecord:query")
    @GetMapping("/{id}")
    public R<BizRechargeRecordVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(bizRechargeRecordService.queryById(id));
    }

    /**
     * 新增充值申请
     */
    @SaCheckPermission("finance:rechargeRecord:add")
    @Log(title = "充值申请", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BizRechargeRecordBo bo) {
        return toAjax(bizRechargeRecordService.insertByBo(bo));
    }

    /**
     * 修改充值申请
     */
    @SaCheckPermission("finance:rechargeRecord:edit")
    @Log(title = "充值申请", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BizRechargeRecordBo bo) {
        return toAjax(bizRechargeRecordService.updateByBo(bo));
    }

    /**
     * 删除充值申请
     *
     * @param ids 主键串
     */
    @SaCheckPermission("finance:rechargeRecord:remove")
    @Log(title = "充值申请", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(bizRechargeRecordService.deleteWithValidByIds(List.of(ids), true));
    }
}
