package org.dromara.insurance.controller;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckLogin;
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
import org.dromara.insurance.domain.vo.InsuranceProductCategoryVo;
import org.dromara.insurance.domain.bo.InsuranceProductCategoryBo;
import org.dromara.insurance.service.IBizInsuranceProductCategoryService;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import cn.hutool.core.lang.tree.Tree;

/**
 * 产品分类管理
 *
 * @author lixiang
 * @date 2026-04-29
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/insurance/insuranceProductCategory")
public class InsuranceProductCategoryController extends BaseController {

    private final IBizInsuranceProductCategoryService bizInsuranceProductCategoryService;

    /**
     * 查询分类下拉树结构
     */
    //@SaCheckPermission("insurance:insuranceProductCategory:list")
    @SaCheckLogin
    @GetMapping("/treeSelect")
    public R<List<Tree<Long>>> treeSelect(InsuranceProductCategoryBo bo) {
        List<Tree<Long>> trees = bizInsuranceProductCategoryService.selectCategoryTreeList(bo);
        return R.ok(trees);
    }

    /**
     * 查询产品分类管理列表
     */
    //@SaCheckPermission("insurance:insuranceProductCategory:list")
    @SaCheckLogin
    @GetMapping("/list")
    public TableDataInfo<InsuranceProductCategoryVo> list(InsuranceProductCategoryBo bo, PageQuery pageQuery) {
        return bizInsuranceProductCategoryService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出产品分类管理列表
     */
    @SaCheckPermission("insurance:insuranceProductCategory:export")
    @Log(title = "产品分类管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(InsuranceProductCategoryBo bo, HttpServletResponse response) {
        List<InsuranceProductCategoryVo> list = bizInsuranceProductCategoryService.queryList(bo);
        ExcelUtil.exportExcel(list, "产品分类管理", InsuranceProductCategoryVo.class, response);
    }

    /**
     * 获取产品分类管理详细信息
     *
     * @param categoryId 主键
     */
    @SaCheckPermission("insurance:insuranceProductCategory:query")
    @GetMapping("/{categoryId}")
    public R<InsuranceProductCategoryVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long categoryId) {
        return R.ok(bizInsuranceProductCategoryService.queryById(categoryId));
    }

    /**
     * 新增产品分类管理
     */
    @SaCheckPermission("insurance:insuranceProductCategory:add")
    @Log(title = "产品分类管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody InsuranceProductCategoryBo bo) {
        return toAjax(bizInsuranceProductCategoryService.insertByBo(bo));
    }

    /**
     * 修改产品分类管理
     */
    @SaCheckPermission("insurance:insuranceProductCategory:edit")
    @Log(title = "产品分类管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody InsuranceProductCategoryBo bo) {
        return toAjax(bizInsuranceProductCategoryService.updateByBo(bo));
    }

    /**
     * 删除产品分类管理
     *
     * @param categoryIds 主键串
     */
    @SaCheckPermission("insurance:insuranceProductCategory:remove")
    @Log(title = "产品分类管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] categoryIds) {
        return toAjax(bizInsuranceProductCategoryService.deleteWithValidByIds(List.of(categoryIds), true));
    }
}
