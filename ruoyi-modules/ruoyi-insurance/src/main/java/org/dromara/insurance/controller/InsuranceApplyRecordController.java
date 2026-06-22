package org.dromara.insurance.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.TenantConstants;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.insurance.domain.dto.PayWithBalanceReqDTO;
import org.dromara.insurance.domain.dto.OrderInsureInfoDTO;
import org.dromara.insurance.domain.dto.VoucherPdfResult;
import org.dromara.insurance.domain.vo.SaveInsureResultVO;
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
import org.dromara.insurance.domain.vo.InsuranceApplyRecordVo;
import org.dromara.insurance.domain.bo.InsuranceApplyRecordBo;
import org.dromara.insurance.service.IInsuranceApplyRecordService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 投保记录
 *
 * @author li.xiang
 * @date 2026-03-13
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/insurance/InsuranceApplyRecord")
public class InsuranceApplyRecordController extends BaseController {

    private final IInsuranceApplyRecordService insuranceApplyRecordService;

    /**
     * 查询投保记录列表
     */
    @SaCheckPermission("insurance:InsuranceApplyRecord:list")
    @GetMapping("/list")
    public TableDataInfo<InsuranceApplyRecordVo> list(InsuranceApplyRecordBo bo, PageQuery pageQuery) {
        return insuranceApplyRecordService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询投保记录产品选项
     */
    @SaCheckPermission("insurance:InsuranceApplyRecord:list")
    @GetMapping("/productOptions")
    public R<List<java.util.Map<String, Object>>> productOptions() {
        return R.ok(insuranceApplyRecordService.queryProductOptions());
    }

    /**
     * 查询批量投保子单列表
     */
    @SaCheckPermission("insurance:InsuranceApplyRecord:list")
    @GetMapping("/subList/{orderNo}")
    public TableDataInfo<InsuranceApplyRecordVo> subList(@PathVariable("orderNo") String orderNo, PageQuery pageQuery) {
        return insuranceApplyRecordService.querySubPageList(orderNo, pageQuery);
    }

    /**
     * 查询批次子单列表 (不分页)
     */
    @SaCheckPermission("insurance:InsuranceApplyRecord:list")
    @GetMapping("/subOrders")
    public R<List<java.util.Map<String, Object>>> subOrders(@NotBlank(message = "批次单号不能为空") String batchOrderNo) {
        return R.ok(insuranceApplyRecordService.querySubOrders(batchOrderNo));
    }

    /**
     * 个人详情 (投被保人信息)
     */
    @SaCheckPermission("insurance:InsuranceApplyRecord:query")
    @GetMapping("/personDetail")
    public R<java.util.Map<String, Object>> personDetail(@NotBlank(message = "订单号不能为空") String orderNo) {
        return R.ok(insuranceApplyRecordService.queryPersonDetail(orderNo));
    }

    /**
     * 生成投保凭证 PDF
     */
    @SaCheckPermission("insurance:InsuranceApplyRecord:query")
    @GetMapping("/voucherPdf/{orderNo}")
    public void voucherPdf(@NotBlank(message = "订单号不能为空") @PathVariable String orderNo,
                           HttpServletResponse response) throws IOException {
        VoucherPdfResult result = insuranceApplyRecordService.generateVoucherPdf(orderNo);
        String encodedFileName = URLEncoder.encode(result.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/pdf");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.setContentLength(result.getContent().length);
        response.getOutputStream().write(result.getContent());
    }

    /**
     * 导出投保记录列表
     */
    @SaCheckPermission("insurance:InsuranceApplyRecord:export")
    @Log(title = "投保记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(InsuranceApplyRecordBo bo, HttpServletResponse response) {
        insuranceApplyRecordService.exportList(bo, response);
    }

    /**
     * 获取投保记录详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("insurance:InsuranceApplyRecord:query")
    @GetMapping("/{id}")
    public R<InsuranceApplyRecordVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(insuranceApplyRecordService.queryById(id));
    }

    /**
     * 新增投保记录
     */
    @SaCheckPermission("insurance:InsuranceApplyRecord:add")
    @Log(title = "投保记录", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody InsuranceApplyRecordBo bo) {
        return toAjax(insuranceApplyRecordService.insertByBo(bo));
    }

    /**
     * 修改投保记录
     */
    @SaCheckPermission("insurance:InsuranceApplyRecord:edit")
    @Log(title = "投保记录", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody InsuranceApplyRecordBo bo) {
        return toAjax(insuranceApplyRecordService.updateByBo(bo));
    }

    /**
     * 删除投保记录
     *
     * @param ids 主键串
     */
    @SaCheckLogin
    @Log(title = "投保记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(insuranceApplyRecordService.deleteWithValidByIds(List.of(ids), true));
    }

    @SaCheckRole(value = {
        "leader",
        TenantConstants.TENANT_ADMIN_ROLE_KEY
    }, mode = SaMode.OR)
    @RepeatSubmit()
    @PostMapping("/confirmPay")
    public R<Void> confirmPay(@RequestBody InsuranceApplyRecordVo insuranceApplyRecordVo){
        return toAjax(insuranceApplyRecordService.handleOrderPaySuccess(insuranceApplyRecordVo.getId()));
    }

    @SaCheckLogin
    @RepeatSubmit()
    @PostMapping("/saveInsureInfo/{orderNo}")
    public R<SaveInsureResultVO> saveInsureInfo(@PathVariable("orderNo") String orderNo,
                                  @RequestBody OrderInsureInfoDTO infoDTO) {
        log.info("orderNo：{}",orderNo);
        log.info("infoDTO：{}", JsonUtils.toJsonString(infoDTO));
        SaveInsureResultVO saveInsureResultVO = insuranceApplyRecordService.saveInsureInfo(orderNo, infoDTO);
        log.info("saveInsureResultVO：{}", JsonUtils.toJsonString(saveInsureResultVO));
        return R.ok(saveInsureResultVO);
    }

    @SaCheckLogin
    @RepeatSubmit()
    @PostMapping("/payWithBalance")
    public R<Void> payWithBalance(@RequestBody PayWithBalanceReqDTO payWithBalanceReqDTO) {
        log.info("payWithBalanceReqDTO：{}",payWithBalanceReqDTO);
        return toAjax(insuranceApplyRecordService.payWithBalance(payWithBalanceReqDTO));
    }

    /**
     * 移动端取消订单
     */
    @SaCheckLogin
    @RepeatSubmit()
    @PostMapping("/cancelOrder")
    public R<Void> cancelOrder(@RequestBody InsuranceApplyRecordBo bo) {
        log.info("移动端请求取消订单：{}", JsonUtils.toJsonString(bo));
        return toAjax(insuranceApplyRecordService.cancelOrder(bo));
    }

}
