package org.dromara.insurance.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.TenantConstants;
import org.dromara.common.core.domain.R;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.insurance.domain.bo.InsurancePolicyBo;
import org.dromara.insurance.domain.vo.InsurancePolicyVo;
import org.dromara.insurance.service.IInsurancePolicyService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 平台承保保单管理（000000 平台管理员跨租户使用）
 *
 * @author li.xiang
 * @date 2026-05-13
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/insurance/InsurancePolicyAdmin")
public class InsurancePolicyAdminController extends BaseController {

    private final IInsurancePolicyService insurancePolicyService;

    /**
     * 查询全部租户承保保单列表
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("insurance:InsurancePolicyAdmin:list")
    @GetMapping("/list")
    public TableDataInfo<InsurancePolicyVo> list(InsurancePolicyBo bo, PageQuery pageQuery) {
        return insurancePolicyService.queryAdminPageList(bo, pageQuery);
    }

    /**
     * 导出全部租户承保保单列表
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("insurance:InsurancePolicyAdmin:export")
    @Log(title = "平台承保保单管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(InsurancePolicyBo bo, HttpServletResponse response) {
        List<InsurancePolicyVo> list = insurancePolicyService.queryAdminList(bo);
        ExcelUtil.exportExcel(list, "平台承保保单", InsurancePolicyVo.class, response);
    }

    /**
     * 获取全部租户承保保单详细信息
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("insurance:InsurancePolicyAdmin:query")
    @GetMapping("/{id}")
    public R<InsurancePolicyVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(insurancePolicyService.queryAdminById(id));
    }
}
