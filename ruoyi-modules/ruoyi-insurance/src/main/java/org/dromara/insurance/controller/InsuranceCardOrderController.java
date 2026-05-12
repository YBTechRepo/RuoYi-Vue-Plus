package org.dromara.insurance.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.insurance.domain.bo.InsuranceCardOrderBo;
import org.dromara.insurance.domain.dto.OrderInsureInfoDTO;
import org.dromara.insurance.domain.dto.PayWithBalanceReqDTO;
import org.dromara.insurance.domain.vo.InsuranceCardOrderVo;
import org.dromara.insurance.domain.vo.SaveInsureResultVO;
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
 * 卡密订单（租户用户查看自己的订单）
 *
 * @author li.xiang
 * @date 2026-05-12
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/insurance/InsuranceCardOrder")
public class InsuranceCardOrderController extends BaseController {

    private final IInsuranceCardOrderService insuranceCardOrderService;

    /**
     * 查询当前登录人的卡密订单列表
     */
    @SaCheckPermission("insurance:cardOrder:list")
    @GetMapping("/list")
    public TableDataInfo<InsuranceCardOrderVo> list(InsuranceCardOrderBo bo, PageQuery pageQuery) {
        return insuranceCardOrderService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出卡密订单列表
     */
    @SaCheckPermission("insurance:cardOrder:export")
    @Log(title = "卡密订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(InsuranceCardOrderBo bo, HttpServletResponse response) {
        List<InsuranceCardOrderVo> list = insuranceCardOrderService.queryList(bo);
        ExcelUtil.exportExcel(list, "卡密订单", InsuranceCardOrderVo.class, response);
    }

    /**
     * 获取当前登录人的卡密订单详细信息
     */
    @SaCheckPermission("insurance:cardOrder:query")
    @GetMapping("/{id}")
    public R<InsuranceCardOrderVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(insuranceCardOrderService.queryById(id));
    }

    /**
     * 根据订单号查询当前登录人的卡密订单，购买流程使用
     */
    @SaCheckLogin
    @GetMapping("/orderNo/{orderNo}")
    public R<InsuranceCardOrderVo> getByOrderNo(@PathVariable("orderNo") String orderNo) {
        return R.ok(insuranceCardOrderService.queryByOrderNoForCurrentUser(orderNo));
    }

    /**
     * 新增卡密订单，购买流程使用
     */
    @SaCheckLogin
    @Log(title = "卡密订单", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody InsuranceCardOrderBo bo) {
        return toAjax(insuranceCardOrderService.insertByBo(bo));
    }

    /**
     * 保存卡密订单收货与购买信息
     */
    @SaCheckLogin
    @RepeatSubmit()
    @PostMapping("/saveOrderInfo/{orderNo}")
    public R<SaveInsureResultVO> saveOrderInfo(@PathVariable("orderNo") String orderNo, @RequestBody OrderInsureInfoDTO infoDTO) {
        return R.ok(insuranceCardOrderService.saveOrderInfo(orderNo, infoDTO));
    }

    /**
     * 卡密订单余额支付，不触发佣金计算
     */
    @SaCheckLogin
    @RepeatSubmit()
    @PostMapping("/payWithBalance")
    public R<Void> payWithBalance(@RequestBody PayWithBalanceReqDTO payWithBalanceReqDTO) {
        return toAjax(insuranceCardOrderService.payWithBalance(payWithBalanceReqDTO));
    }
}


