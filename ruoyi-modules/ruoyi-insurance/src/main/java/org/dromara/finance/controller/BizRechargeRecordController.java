package org.dromara.finance.controller;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.finance.domain.RechargeApplyReqDTO;
import org.dromara.finance.domain.RechargeAuditReqDTO;
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
import org.dromara.finance.domain.vo.BizRechargeRecordVo;
import org.dromara.finance.domain.bo.BizRechargeRecordBo;
import org.dromara.finance.service.IBizRechargeRecordService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 充值申请
 *
 * @author li.xiang
 * @date 2026-03-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/finance/rechargeRecord")
@Slf4j
public class BizRechargeRecordController extends BaseController {

    private final IBizRechargeRecordService bizRechargeRecordService;

    /**
     * 销售端充值申请
     */
    @SaCheckLogin
    @RepeatSubmit()
    @PostMapping("/rechargeApply")
    public R<Void> rechargeApply(@RequestBody RechargeApplyReqDTO rechargeApplyReqDTO) {
        boolean success = bizRechargeRecordService.applyRecharge(rechargeApplyReqDTO);
        return success ? R.ok() : R.fail("充值申请提交失败");
    }

    /**
     * 充值审批
     */
    @SaCheckLogin
    @RepeatSubmit()
    @PostMapping("/rechargeAudit")
    public R<Void> rechargeAudit(@RequestBody RechargeAuditReqDTO rechargeAuditReqDTO) {
        log.info("审核请求参数：{}", JsonUtils.toJsonString(rechargeAuditReqDTO));
        return toAjax(bizRechargeRecordService.auditRecharge(rechargeAuditReqDTO));
    }

    /**
     * 查询充值申请列表
     */
    @SaCheckPermission("finance:rechargeRecord:list")
    @GetMapping("/list")
    public TableDataInfo<BizRechargeRecordVo> list(BizRechargeRecordBo bo, PageQuery pageQuery) {
//        return bizRechargeRecordService.queryPageList(bo, pageQuery);
        // 调用超管专用的 Service 方法
        return bizRechargeRecordService.queryAdminPageList(bo, pageQuery);
    }

    /**
     * 导出充值申请列表
     */
    @SaCheckPermission("finance:rechargeRecord:export")
    @Log(title = "充值申请", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BizRechargeRecordBo bo, HttpServletResponse response) {
        List<BizRechargeRecordVo> list = bizRechargeRecordService.queryAdminList(bo);
        ExcelUtil.exportExcel(list, "充值申请", BizRechargeRecordVo.class, response);
    }

    /**
     * 获取充值申请详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("finance:rechargeRecord:query")
    @GetMapping("/{id}")
    public R<BizRechargeRecordVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(bizRechargeRecordService.queryAdminById(id));
    }

    /**
     * 新增充值申请
     */
    @SaCheckPermission("finance:rechargeRecord:add")
    @Log(title = "充值申请", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BizRechargeRecordBo bo) {
        return toAjax(bizRechargeRecordService.insertByBo(bo));
    }

    /**
     * 修改充值申请
     */
    @SaCheckPermission("finance:rechargeRecord:edit")
    @Log(title = "充值申请", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BizRechargeRecordBo bo) {
        return toAjax(bizRechargeRecordService.updateByBo(bo));
    }

    /**
     * 删除充值申请
     *
     * @param ids 主键串
     */
    @SaCheckPermission("finance:rechargeRecord:remove")
    @Log(title = "充值申请", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(bizRechargeRecordService.deleteWithValidByIds(List.of(ids), true));
    }
}
