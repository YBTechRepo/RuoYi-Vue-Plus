package org.dromara.insurance.controller;

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
import org.dromara.insurance.domain.vo.InsuranceProductCommissionVo;
import org.dromara.insurance.domain.bo.InsuranceProductCommissionBo;
import org.dromara.insurance.service.IInsuranceProductCommissionService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 佣金配置
 *
 * @author li.xiang
 * @date 2026-03-06
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/insurance/InsuranceProductCommission")
public class InsuranceProductCommissionController extends BaseController {

    private final IInsuranceProductCommissionService insuranceProductCommissionService;

    /**
     * 查询佣金配置列表
     */
    @SaCheckPermission("insurance:InsuranceProductCommission:list")
    @GetMapping("/list")
    public TableDataInfo<InsuranceProductCommissionVo> list(InsuranceProductCommissionBo bo, PageQuery pageQuery) {
        return insuranceProductCommissionService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出佣金配置列表
     */
    @SaCheckPermission("insurance:InsuranceProductCommission:export")
    @Log(title = "佣金配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(InsuranceProductCommissionBo bo, HttpServletResponse response) {
        List<InsuranceProductCommissionVo> list = insuranceProductCommissionService.queryList(bo);
        ExcelUtil.exportExcel(list, "佣金配置", InsuranceProductCommissionVo.class, response);
    }

    /**
     * 获取佣金配置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("insurance:InsuranceProductCommission:query")
    @GetMapping("/{id}")
    public R<InsuranceProductCommissionVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(insuranceProductCommissionService.queryById(id));
    }

    /**
     * 新增佣金配置
     */
    @SaCheckPermission("insurance:InsuranceProductCommission:add")
    @Log(title = "佣金配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody InsuranceProductCommissionBo bo) {
        return toAjax(insuranceProductCommissionService.insertByBo(bo));
    }

    /**
     * 修改佣金配置
     */
    @SaCheckPermission("insurance:InsuranceProductCommission:edit")
    @Log(title = "佣金配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody InsuranceProductCommissionBo bo) {
        return toAjax(insuranceProductCommissionService.updateByBo(bo));
    }

    /**
     * 删除佣金配置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("insurance:InsuranceProductCommission:remove")
    @Log(title = "佣金配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(insuranceProductCommissionService.deleteWithValidByIds(List.of(ids), true));
    }
}
