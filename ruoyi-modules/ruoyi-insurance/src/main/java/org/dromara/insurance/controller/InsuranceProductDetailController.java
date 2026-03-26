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
import org.dromara.insurance.domain.vo.InsuranceProductDetailVo;
import org.dromara.insurance.domain.bo.InsuranceProductDetailBo;
import org.dromara.insurance.service.IInsuranceProductDetailService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 保险产品-图文详情
 *
 * @author li.xiang
 * @date 2026-03-23
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/insurance/InsuranceProductDetail")
public class InsuranceProductDetailController extends BaseController {

    private final IInsuranceProductDetailService insuranceProductDetailService;

    /**
     * 查询保险产品-图文详情列表
     */
    @SaCheckPermission("insurance:InsuranceProductDetail:list")
    @GetMapping("/list")
    public TableDataInfo<InsuranceProductDetailVo> list(InsuranceProductDetailBo bo, PageQuery pageQuery) {
        return insuranceProductDetailService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出保险产品-图文详情列表
     */
    @SaCheckPermission("insurance:InsuranceProductDetail:export")
    @Log(title = "保险产品-图文详情", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(InsuranceProductDetailBo bo, HttpServletResponse response) {
        List<InsuranceProductDetailVo> list = insuranceProductDetailService.queryList(bo);
        ExcelUtil.exportExcel(list, "保险产品-图文详情", InsuranceProductDetailVo.class, response);
    }

    /**
     * 获取保险产品-图文详情详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("insurance:InsuranceProductDetail:query")
    @GetMapping("/{id}")
    public R<InsuranceProductDetailVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(insuranceProductDetailService.queryById(id));
    }

    /**
     * 新增保险产品-图文详情
     */
    @SaCheckPermission("insurance:InsuranceProductDetail:add")
    @Log(title = "保险产品-图文详情", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody InsuranceProductDetailBo bo) {
        return toAjax(insuranceProductDetailService.insertByBo(bo));
    }

    /**
     * 修改保险产品-图文详情
     */
    @SaCheckPermission("insurance:InsuranceProductDetail:edit")
    @Log(title = "保险产品-图文详情", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody InsuranceProductDetailBo bo) {
        return toAjax(insuranceProductDetailService.updateByBo(bo));
    }

    /**
     * 删除保险产品-图文详情
     *
     * @param ids 主键串
     */
    @SaCheckPermission("insurance:InsuranceProductDetail:remove")
    @Log(title = "保险产品-图文详情", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(insuranceProductDetailService.deleteWithValidByIds(List.of(ids), true));
    }
}
