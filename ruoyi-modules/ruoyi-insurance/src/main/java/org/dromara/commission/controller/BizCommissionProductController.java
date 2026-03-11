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
import org.dromara.commission.domain.vo.BizCommissionProductVo;
import org.dromara.commission.domain.bo.BizCommissionProductBo;
import org.dromara.commission.service.IBizCommissionProductService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 特殊产品费率配置
 *
 * @author li.xiang
 * @date 2026-03-09
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/commission/CommissionProduct")
public class BizCommissionProductController extends BaseController {

    private final IBizCommissionProductService bizCommissionProductService;

    /**
     * 查询特殊产品费率配置列表
     */
    @SaCheckPermission("commission:CommissionProduct:list")
    @GetMapping("/list")
    public TableDataInfo<BizCommissionProductVo> list(BizCommissionProductBo bo, PageQuery pageQuery) {
        return bizCommissionProductService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出特殊产品费率配置列表
     */
    @SaCheckPermission("commission:CommissionProduct:export")
    @Log(title = "特殊产品费率配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BizCommissionProductBo bo, HttpServletResponse response) {
        List<BizCommissionProductVo> list = bizCommissionProductService.queryList(bo);
        ExcelUtil.exportExcel(list, "特殊产品费率配置", BizCommissionProductVo.class, response);
    }

    /**
     * 获取特殊产品费率配置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("commission:CommissionProduct:query")
    @GetMapping("/{id}")
    public R<BizCommissionProductVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(bizCommissionProductService.queryById(id));
    }

    /**
     * 新增特殊产品费率配置
     */
    @SaCheckPermission("commission:CommissionProduct:add")
    @Log(title = "特殊产品费率配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BizCommissionProductBo bo) {
        return toAjax(bizCommissionProductService.insertByBo(bo));
    }

    /**
     * 修改特殊产品费率配置
     */
    @SaCheckPermission("commission:CommissionProduct:edit")
    @Log(title = "特殊产品费率配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BizCommissionProductBo bo) {
        return toAjax(bizCommissionProductService.updateByBo(bo));
    }

    /**
     * 删除特殊产品费率配置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("commission:CommissionProduct:remove")
    @Log(title = "特殊产品费率配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(bizCommissionProductService.deleteWithValidByIds(List.of(ids), true));
    }
}
