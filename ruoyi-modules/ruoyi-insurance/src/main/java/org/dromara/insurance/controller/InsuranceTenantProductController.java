package org.dromara.insurance.controller;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
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
import org.dromara.insurance.domain.vo.InsuranceTenantProductVo;
import org.dromara.insurance.domain.bo.InsuranceTenantProductBo;
import org.dromara.insurance.service.IInsuranceTenantProductService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 产品库
 *
 * @author li.xiang
 * @date 2026-03-20
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/insurance/InsuranceTenantProduct")
public class InsuranceTenantProductController extends BaseController {

    private final IInsuranceTenantProductService insuranceTenantProductService;

    /**
     * 查询产品库列表
     */
    @SaCheckPermission("insurance:InsuranceTenantProduct:list")
    @GetMapping("/list")
    public TableDataInfo<InsuranceTenantProductVo> list(InsuranceTenantProductBo bo, PageQuery pageQuery) {
        return insuranceTenantProductService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出产品库列表
     */
    @SaCheckPermission("insurance:InsuranceTenantProduct:export")
    @Log(title = "产品库", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(InsuranceTenantProductBo bo, HttpServletResponse response) {
        List<InsuranceTenantProductVo> list = insuranceTenantProductService.queryList(bo);
        ExcelUtil.exportExcel(list, "产品库", InsuranceTenantProductVo.class, response);
    }

    /**
     * 获取产品库详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("insurance:InsuranceTenantProduct:query")
    @GetMapping("/{id}")
    public R<InsuranceTenantProductVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(insuranceTenantProductService.queryById(id));
    }

    /**
     * 新增产品库
     */
    @SaCheckPermission("insurance:InsuranceTenantProduct:add")
    @Log(title = "产品库", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody InsuranceTenantProductBo bo) {
        return toAjax(insuranceTenantProductService.insertByBo(bo));
    }

    @SaCheckPermission("insurance:InsuranceTenantProduct:add")
    @Log(title = "产品库-批量添加", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/batchAdd") // 建议新开一个专门的批量接口路由
    public R<Void> batchAdd(@RequestBody List<Long> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return R.fail("请至少选择一个产品进行添加");
        }
        return toAjax(insuranceTenantProductService.batchAddProducts(productIds));
    }

    /**
     * 修改产品库
     */
    @SaCheckPermission("insurance:InsuranceTenantProduct:edit")
    @Log(title = "产品库", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody InsuranceTenantProductBo bo) {
        return toAjax(insuranceTenantProductService.updateByBo(bo));
    }

    /**
     * 删除产品库
     *
     * @param ids 主键串
     */
    @SaCheckPermission("insurance:InsuranceTenantProduct:remove")
    @Log(title = "产品库", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(insuranceTenantProductService.deleteWithValidByIds(List.of(ids), true));
    }
}
