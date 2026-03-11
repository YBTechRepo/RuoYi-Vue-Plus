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
import org.dromara.commission.domain.vo.BizCommissionDeptVo;
import org.dromara.commission.domain.bo.BizCommissionDeptBo;
import org.dromara.commission.service.IBizCommissionDeptService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 机构费率配置
 *
 * @author li.xiang
 * @date 2026-03-09
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/commission/CommissionDept")
public class BizCommissionDeptController extends BaseController {

    private final IBizCommissionDeptService bizCommissionDeptService;

    /**
     * 查询机构费率配置列表
     */
    @SaCheckPermission("commission:CommissionDept:list")
    @GetMapping("/list")
    public TableDataInfo<BizCommissionDeptVo> list(BizCommissionDeptBo bo, PageQuery pageQuery) {
        return bizCommissionDeptService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出机构费率配置列表
     */
    @SaCheckPermission("commission:CommissionDept:export")
    @Log(title = "机构费率配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BizCommissionDeptBo bo, HttpServletResponse response) {
        List<BizCommissionDeptVo> list = bizCommissionDeptService.queryList(bo);
        ExcelUtil.exportExcel(list, "机构费率配置", BizCommissionDeptVo.class, response);
    }

    /**
     * 获取机构费率配置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("commission:CommissionDept:query")
    @GetMapping("/{id}")
    public R<BizCommissionDeptVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(bizCommissionDeptService.queryById(id));
    }

    /**
     * 新增机构费率配置
     */
    @SaCheckPermission("commission:CommissionDept:add")
    @Log(title = "机构费率配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BizCommissionDeptBo bo) {
        return toAjax(bizCommissionDeptService.insertByBo(bo));
    }

    /**
     * 修改机构费率配置
     */
    @SaCheckPermission("commission:CommissionDept:edit")
    @Log(title = "机构费率配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BizCommissionDeptBo bo) {
        return toAjax(bizCommissionDeptService.updateByBo(bo));
    }

    /**
     * 删除机构费率配置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("commission:CommissionDept:remove")
    @Log(title = "机构费率配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(bizCommissionDeptService.deleteWithValidByIds(List.of(ids), true));
    }
}
