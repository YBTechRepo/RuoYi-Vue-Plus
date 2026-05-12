package org.dromara.insurance.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.TenantConstants;
import org.dromara.common.core.domain.R;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.insurance.domain.bo.InsuranceCardOrderBo;
import org.dromara.insurance.domain.vo.InsuranceCardOrderVo;
import org.dromara.insurance.service.IInsuranceCardOrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 平台卡密订单管理（000000 平台管理员跨租户使用）
 *
 * @author li.xiang
 * @date 2026-05-12
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/insurance/InsuranceCardOrderAdmin")
public class InsuranceCardOrderAdminController extends BaseController {

    private final IInsuranceCardOrderService insuranceCardOrderService;

    /**
     * 查询全部租户卡密订单列表
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("insurance:cardOrderAdmin:list")
    @GetMapping("/list")
    public TableDataInfo<InsuranceCardOrderVo> list(InsuranceCardOrderBo bo, PageQuery pageQuery) {
        return insuranceCardOrderService.queryAdminPageList(bo, pageQuery);
    }

    /**
     * 导出全部租户卡密订单列表
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("insurance:cardOrderAdmin:export")
    @Log(title = "平台卡密订单管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(InsuranceCardOrderBo bo, HttpServletResponse response) {
        List<InsuranceCardOrderVo> list = insuranceCardOrderService.queryAdminList(bo);
        ExcelUtil.exportExcel(list, "平台卡密订单", InsuranceCardOrderVo.class, response);
    }

    /**
     * 获取全部租户卡密订单详细信息
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("insurance:cardOrderAdmin:query")
    @GetMapping("/{id}")
    public R<InsuranceCardOrderVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(insuranceCardOrderService.queryAdminById(id));
    }

    /**
     * 录入或修改快递信息
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("insurance:cardOrderAdmin:edit")
    @Log(title = "平台卡密订单管理-快递信息", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/logistics")
    public R<Void> logistics(@RequestBody InsuranceCardOrderBo bo) {
        return toAjax(insuranceCardOrderService.updateLogistics(bo));
    }
}
