package org.dromara.insurance.controller;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.dromara.common.core.constant.TenantConstants;
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
import org.dromara.insurance.domain.vo.InsuranceApplyRecordVo;
import org.dromara.insurance.domain.bo.InsuranceApplyRecordBo;
import org.dromara.insurance.service.IInsuranceProxyOrderService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 代投保订单查询 (Admin全局视图)
 *
 * @author li.xiang
 * @date 2026-04-14
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/insurance/insuranceProxyOrder")
@SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY) // 仅限超管使用
public class InsuranceProxyOrderController extends BaseController {

    private final IInsuranceProxyOrderService insuranceProxyOrderService;

    /**
     * 查询代投保订单查询列表
     */
    @SaCheckPermission("insurance:insuranceProxyOrder:list")
    @GetMapping("/list")
    public TableDataInfo<InsuranceApplyRecordVo> list(InsuranceApplyRecordBo bo, PageQuery pageQuery) {
        return insuranceProxyOrderService.queryPageList(bo, pageQuery);
    }

    /**
     * 批次子单列表
     */
    @SaCheckPermission("insurance:insuranceProxyOrder:list")
    @GetMapping("/subOrders")
    public R<List<Map<String, Object>>> subOrders(@NotBlank(message = "批次单号不能为空") String batchOrderNo) {
        return R.ok(insuranceProxyOrderService.querySubOrders(batchOrderNo));
    }

    /**
     * 个人详情 (投被保人信息)
     */
    @SaCheckPermission("insurance:insuranceProxyOrder:query")
    @GetMapping("/personDetail")
    public R<Map<String, Object>> personDetail(@NotBlank(message = "订单号不能为空") String orderNo) {
        return R.ok(insuranceProxyOrderService.queryPersonDetail(orderNo));
    }

    /**
     * 导出代投保订单查询列表
     */
    @SaCheckPermission("insurance:insuranceProxyOrder:export")
    @Log(title = "代投保订单查询", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(InsuranceApplyRecordBo bo, HttpServletResponse response) {
        insuranceProxyOrderService.exportList(bo, response);
    }

    /**
     * 获取代投保订单查询详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("insurance:insuranceProxyOrder:query")
    @GetMapping("/{id}")
    public R<InsuranceApplyRecordVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(insuranceProxyOrderService.queryById(id));
    }

    /**
     * 新增代投保订单查询
     */
    @SaCheckPermission("insurance:insuranceProxyOrder:add")
    @Log(title = "代投保订单查询", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody InsuranceApplyRecordBo bo) {
        return toAjax(insuranceProxyOrderService.insertByBo(bo));
    }

    /**
     * 修改代投保订单查询
     */
    @SaCheckPermission("insurance:insuranceProxyOrder:edit")
    @Log(title = "代投保订单查询", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody InsuranceApplyRecordBo bo) {
        return toAjax(insuranceProxyOrderService.updateByBo(bo));
    }

    /**
     * 管理员取消代投保订单
     */
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("insurance:insuranceProxyOrder:edit")
    @Log(title = "代投保订单取消", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody InsuranceApplyRecordBo bo) {
        return toAjax(insuranceProxyOrderService.changeStatus(bo));
    }

    /**
     * 删除代投保订单查询
     *
     * @param ids 主键串
     */
    @SaCheckPermission("insurance:insuranceProxyOrder:remove")
    @Log(title = "代投保订单查询", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(insuranceProxyOrderService.deleteWithValidByIds(List.of(ids), true));
    }
}
