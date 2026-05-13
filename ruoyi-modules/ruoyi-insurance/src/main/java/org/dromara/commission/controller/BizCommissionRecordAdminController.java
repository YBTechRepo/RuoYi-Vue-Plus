package org.dromara.commission.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.commission.domain.bo.BizCommissionRecordBo;
import org.dromara.commission.domain.vo.BizCommissionRecordVo;
import org.dromara.commission.service.IBizCommissionRecordService;
import org.dromara.common.core.constant.TenantConstants;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 平台佣金分配明细管理（000000 平台管理员跨租户使用）
 *
 * @author li.xiang
 * @date 2026-05-13
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/commission/CommissionRecordAdmin")
public class BizCommissionRecordAdminController extends BaseController {

    private final IBizCommissionRecordService bizCommissionRecordService;

    /**
     * 查询全部租户佣金分配明细列表
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("commission:CommissionRecordAdmin:list")
    @GetMapping("/list")
    public TableDataInfo<BizCommissionRecordVo> list(BizCommissionRecordBo bo, PageQuery pageQuery) {
        return bizCommissionRecordService.queryAdminPageList(bo, pageQuery);
    }

    /**
     * 导出全部租户佣金分配明细列表
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("commission:CommissionRecordAdmin:export")
    @Log(title = "租户佣金明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BizCommissionRecordBo bo, HttpServletResponse response) {
        List<BizCommissionRecordVo> list = bizCommissionRecordService.queryAdminList(bo);
        ExcelUtil.exportExcel(list, "租户佣金明细", BizCommissionRecordVo.class, response);
    }
}
