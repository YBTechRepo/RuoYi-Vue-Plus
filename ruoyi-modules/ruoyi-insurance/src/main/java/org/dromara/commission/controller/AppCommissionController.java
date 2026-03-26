package org.dromara.commission.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.commission.domain.bo.AppCommissionQueryBo;
import org.dromara.commission.domain.vo.AppCommissionItemVo;
import org.dromara.commission.domain.vo.CommissionSummaryVo;
import org.dromara.commission.service.IBizCommissionRecordService;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/open/api/v1/commission")
@RequiredArgsConstructor
@Slf4j
public class AppCommissionController {

    private final IBizCommissionRecordService commissionService;

    /**
     * 1. 获取佣金头部汇总 (支持按月联动)
     */
    @GetMapping("/summary")
    @SaCheckLogin
    public R<CommissionSummaryVo> getSummary(@RequestParam(value = "queryMonth", required = false) String queryMonth) {
        // 从 Sa-Token 获取当前登录的业务员 ID
        Long userId = LoginHelper.getUserId();
        CommissionSummaryVo summary = commissionService.getAppCommissionSummary(userId, queryMonth);
        return R.ok(summary);
    }

    /**
     * 2. 获取佣金分页明细列表
     */
    @GetMapping("/list")
    @SaCheckLogin
    public TableDataInfo<AppCommissionItemVo> getList(@Validated AppCommissionQueryBo bo, PageQuery pageQuery) {
        Long userId = LoginHelper.getUserId();
        return commissionService.queryAppCommissionPageList(bo, pageQuery, userId);
    }
}
