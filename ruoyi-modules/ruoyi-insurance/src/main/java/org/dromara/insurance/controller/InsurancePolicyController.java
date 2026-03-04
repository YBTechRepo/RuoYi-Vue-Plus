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
import org.dromara.insurance.domain.vo.InsurancePolicyVo;
import org.dromara.insurance.domain.bo.InsurancePolicyBo;
import org.dromara.insurance.service.IInsurancePolicyService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 承保保单
 *
 * @author li.xiang
 * @date 2026-03-02
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/insurance/InsurancePolicy")
public class InsurancePolicyController extends BaseController {

    private final IInsurancePolicyService insurancePolicyService;

    /**
     * 查询承保保单列表
     */
    @SaCheckPermission("insurance:InsurancePolicy:list")
    @GetMapping("/list")
    public TableDataInfo<InsurancePolicyVo> list(InsurancePolicyBo bo, PageQuery pageQuery) {
        return insurancePolicyService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出承保保单列表
     */
    @SaCheckPermission("insurance:InsurancePolicy:export")
    @Log(title = "承保保单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(InsurancePolicyBo bo, HttpServletResponse response) {
        List<InsurancePolicyVo> list = insurancePolicyService.queryList(bo);
        ExcelUtil.exportExcel(list, "承保保单", InsurancePolicyVo.class, response);
    }

    /**
     * 获取承保保单详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("insurance:InsurancePolicy:query")
    @GetMapping("/{id}")
    public R<InsurancePolicyVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(insurancePolicyService.queryById(id));
    }

    /**
     * 新增承保保单
     */
    @SaCheckPermission("insurance:InsurancePolicy:add")
    @Log(title = "承保保单", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody InsurancePolicyBo bo) {
        return toAjax(insurancePolicyService.insertByBo(bo));
    }

    /**
     * 修改承保保单
     */
    @SaCheckPermission("insurance:InsurancePolicy:edit")
    @Log(title = "承保保单", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody InsurancePolicyBo bo) {
        return toAjax(insurancePolicyService.updateByBo(bo));
    }

    /**
     * 删除承保保单
     *
     * @param ids 主键串
     */
    @SaCheckPermission("insurance:InsurancePolicy:remove")
    @Log(title = "承保保单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(insurancePolicyService.deleteWithValidByIds(List.of(ids), true));
    }
}
