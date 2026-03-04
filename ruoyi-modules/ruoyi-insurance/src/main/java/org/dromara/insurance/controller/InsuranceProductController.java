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
import org.dromara.insurance.domain.vo.InsuranceProductVo;
import org.dromara.insurance.domain.bo.InsuranceProductBo;
import org.dromara.insurance.service.IInsuranceProductService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 产品配置
 *
 * @author li.xiang
 * @date 2026-03-02
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/insurance/InsuranceProduct")
public class InsuranceProductController extends BaseController {

    private final IInsuranceProductService insuranceProductService;

    /**
     * 查询产品配置列表
     */
    @SaCheckPermission("insurance:InsuranceProduct:list")
    @GetMapping("/list")
    public TableDataInfo<InsuranceProductVo> list(InsuranceProductBo bo, PageQuery pageQuery) {
        return insuranceProductService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出产品配置列表
     */
    @SaCheckPermission("insurance:InsuranceProduct:export")
    @Log(title = "产品配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(InsuranceProductBo bo, HttpServletResponse response) {
        List<InsuranceProductVo> list = insuranceProductService.queryList(bo);
        ExcelUtil.exportExcel(list, "产品配置", InsuranceProductVo.class, response);
    }

    /**
     * 获取产品配置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("insurance:InsuranceProduct:query")
    @GetMapping("/{id}")
    public R<InsuranceProductVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(insuranceProductService.queryById(id));
    }

    /**
     * 新增产品配置
     */
    @SaCheckPermission("insurance:InsuranceProduct:add")
    @Log(title = "产品配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody InsuranceProductBo bo) {
        return toAjax(insuranceProductService.insertByBo(bo));
    }

    /**
     * 修改产品配置
     */
    @SaCheckPermission("insurance:InsuranceProduct:edit")
    @Log(title = "产品配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody InsuranceProductBo bo) {
        return toAjax(insuranceProductService.updateByBo(bo));
    }

    /**
     * 删除产品配置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("insurance:InsuranceProduct:remove")
    @Log(title = "产品配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(insuranceProductService.deleteWithValidByIds(List.of(ids), true));
    }
}
