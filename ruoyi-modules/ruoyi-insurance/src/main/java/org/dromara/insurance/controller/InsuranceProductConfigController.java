package org.dromara.insurance.controller;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.insurance.domain.bo.InsuranceProductSaveBo;
import org.dromara.insurance.domain.vo.MarketProductVo;
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
import org.dromara.insurance.domain.vo.InsuranceProductConfigVo;
import org.dromara.insurance.domain.bo.InsuranceProductConfigBo;
import org.dromara.insurance.service.IInsuranceProductConfigService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 产品配置
 *
 * @author li.xiang
 * @date 2026-03-06
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/insurance/InsuranceProductConfig")
public class InsuranceProductConfigController extends BaseController {

    private final IInsuranceProductConfigService insuranceProductConfigService;

    /**
     * 查询产品配置列表
     */
    @SaCheckPermission("insurance:InsuranceProductConfig:list")
    @GetMapping("/list")
    public TableDataInfo<InsuranceProductConfigVo> list(InsuranceProductConfigBo bo, PageQuery pageQuery) {
        return insuranceProductConfigService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出产品配置列表
     */
    @SaCheckPermission("insurance:InsuranceProductConfig:export")
    @Log(title = "产品配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(InsuranceProductConfigBo bo, HttpServletResponse response) {
        List<InsuranceProductConfigVo> list = insuranceProductConfigService.queryList(bo);
        ExcelUtil.exportExcel(list, "产品配置", InsuranceProductConfigVo.class, response);
    }

    /**
     * 获取授权产品
     */
    @SaCheckLogin
    @GetMapping("/marketList")
    public TableDataInfo<MarketProductVo> marketList(InsuranceProductConfigBo bo,PageQuery pageQuery) {
        return insuranceProductConfigService.queryMarketPageList(bo, pageQuery);
    }

    /**
     * 获取产品配置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("insurance:InsuranceProductConfig:query")
    @GetMapping("/{id}")
    public R<InsuranceProductConfigVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(insuranceProductConfigService.queryById(id));
    }

    /**
     * 新增产品配置
     */
    @SaCheckPermission("insurance:InsuranceProductConfig:add")
    @Log(title = "产品配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody InsuranceProductConfigBo bo) {
        return toAjax(insuranceProductConfigService.insertByBo(bo));
    }

    /**
     * 修改产品配置
     */
    @SaCheckPermission("insurance:InsuranceProductConfig:edit")
    @Log(title = "产品配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody InsuranceProductConfigBo bo) {
        return toAjax(insuranceProductConfigService.updateByBo(bo));
    }

    /**
     * 删除产品配置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("insurance:InsuranceProductConfig:remove")
    @Log(title = "产品配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(insuranceProductConfigService.deleteWithValidByIds(List.of(ids), true));
    }

    /**
     * 获取完整保险产品详细信息 (用于编辑回显)
     */
//    @SaCheckPermission("insurance:productConfig:query")
    @SaCheckLogin
    @GetMapping("/getFull/{id}")
    public R<InsuranceProductSaveBo> getFullInfo(@PathVariable("id") Long id) {
        return R.ok(insuranceProductConfigService.getProductFull(id));
    }

    /**
     * 保存完整保险产品 (包含新增和修改)
     */
    @SaCheckPermission("insurance:productConfig:add")
    @Log(title = "保存完整保险产品", businessType = BusinessType.INSERT)
    @PostMapping("/saveFull")
    public R<Void> saveFull(@Validated @RequestBody InsuranceProductSaveBo bo) {
        insuranceProductConfigService.saveFullProduct(bo);
        return R.ok();
    }
}
