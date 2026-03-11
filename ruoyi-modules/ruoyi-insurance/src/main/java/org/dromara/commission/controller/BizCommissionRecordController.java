package org.dromara.commission.controller;

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
import org.dromara.commission.domain.vo.BizCommissionRecordVo;
import org.dromara.commission.domain.bo.BizCommissionRecordBo;
import org.dromara.commission.service.IBizCommissionRecordService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 佣金分配明细
 *
 * @author li.xiang
 * @date 2026-03-11
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/commission/CommissionRecord")
public class BizCommissionRecordController extends BaseController {

    private final IBizCommissionRecordService bizCommissionRecordService;

    /**
     * 查询佣金分配明细列表
     */
    @SaCheckPermission("commission:CommissionRecord:list")
    @GetMapping("/list")
    public TableDataInfo<BizCommissionRecordVo> list(BizCommissionRecordBo bo, PageQuery pageQuery) {
        return bizCommissionRecordService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出佣金分配明细列表
     */
    @SaCheckPermission("commission:CommissionRecord:export")
    @Log(title = "佣金分配明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BizCommissionRecordBo bo, HttpServletResponse response) {
        List<BizCommissionRecordVo> list = bizCommissionRecordService.queryList(bo);
        ExcelUtil.exportExcel(list, "佣金分配明细", BizCommissionRecordVo.class, response);
    }

    /**
     * 获取佣金分配明细详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("commission:CommissionRecord:query")
    @GetMapping("/{id}")
    public R<BizCommissionRecordVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(bizCommissionRecordService.queryById(id));
    }

    /**
     * 新增佣金分配明细
     */
    @SaCheckPermission("commission:CommissionRecord:add")
    @Log(title = "佣金分配明细", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BizCommissionRecordBo bo) {
        return toAjax(bizCommissionRecordService.insertByBo(bo));
    }

    /**
     * 修改佣金分配明细
     */
    @SaCheckPermission("commission:CommissionRecord:edit")
    @Log(title = "佣金分配明细", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BizCommissionRecordBo bo) {
        return toAjax(bizCommissionRecordService.updateByBo(bo));
    }

    /**
     * 删除佣金分配明细
     *
     * @param ids 主键串
     */
    @SaCheckPermission("commission:CommissionRecord:remove")
    @Log(title = "佣金分配明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(bizCommissionRecordService.deleteWithValidByIds(List.of(ids), true));
    }
}
