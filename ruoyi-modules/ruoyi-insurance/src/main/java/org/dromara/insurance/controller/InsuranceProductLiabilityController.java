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
import org.dromara.insurance.domain.vo.InsuranceProductLiabilityVo;
import org.dromara.insurance.domain.bo.InsuranceProductLiabilityBo;
import org.dromara.insurance.service.IInsuranceProductLiabilityService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 保险产品-保障责任
 *
 * @author li.xiang
 * @date 2026-03-23
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/insurance/InsuranceProductLiability")
public class InsuranceProductLiabilityController extends BaseController {

    private final IInsuranceProductLiabilityService insuranceProductLiabilityService;

    /**
     * 查询保险产品-保障责任列表
     */
    @SaCheckPermission("insurance:InsuranceProductLiability:list")
    @GetMapping("/list")
    public TableDataInfo<InsuranceProductLiabilityVo> list(InsuranceProductLiabilityBo bo, PageQuery pageQuery) {
        return insuranceProductLiabilityService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出保险产品-保障责任列表
     */
    @SaCheckPermission("insurance:InsuranceProductLiability:export")
    @Log(title = "保险产品-保障责任", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(InsuranceProductLiabilityBo bo, HttpServletResponse response) {
        List<InsuranceProductLiabilityVo> list = insuranceProductLiabilityService.queryList(bo);
        ExcelUtil.exportExcel(list, "保险产品-保障责任", InsuranceProductLiabilityVo.class, response);
    }

    /**
     * 获取保险产品-保障责任详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("insurance:InsuranceProductLiability:query")
    @GetMapping("/{id}")
    public R<InsuranceProductLiabilityVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(insuranceProductLiabilityService.queryById(id));
    }

    /**
     * 新增保险产品-保障责任
     */
    @SaCheckPermission("insurance:InsuranceProductLiability:add")
    @Log(title = "保险产品-保障责任", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody InsuranceProductLiabilityBo bo) {
        return toAjax(insuranceProductLiabilityService.insertByBo(bo));
    }

    /**
     * 修改保险产品-保障责任
     */
    @SaCheckPermission("insurance:InsuranceProductLiability:edit")
    @Log(title = "保险产品-保障责任", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody InsuranceProductLiabilityBo bo) {
        return toAjax(insuranceProductLiabilityService.updateByBo(bo));
    }

    /**
     * 删除保险产品-保障责任
     *
     * @param ids 主键串
     */
    @SaCheckPermission("insurance:InsuranceProductLiability:remove")
    @Log(title = "保险产品-保障责任", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(insuranceProductLiabilityService.deleteWithValidByIds(List.of(ids), true));
    }
}
