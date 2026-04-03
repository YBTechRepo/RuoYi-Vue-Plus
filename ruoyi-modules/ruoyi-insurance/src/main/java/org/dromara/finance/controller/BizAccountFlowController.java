package org.dromara.finance.controller;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.finance.domain.AccountAdjustReqDTO;
import org.dromara.finance.service.IBizUserAccountService;
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
import org.dromara.finance.domain.vo.BizAccountFlowVo;
import org.dromara.finance.domain.bo.BizAccountFlowBo;
import org.dromara.finance.service.IBizAccountFlowService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 账户明细
 *
 * @author li.xiang
 * @date 2026-03-27
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/finance/accountFlow")
public class BizAccountFlowController extends BaseController {

    private final IBizAccountFlowService bizAccountFlowService;

    private final IBizUserAccountService bizUserAccountService;

    /**
     * 查询账户明细列表
     */
    @SaCheckPermission("finance:accountFlow:list")
    @GetMapping("/list")
    public TableDataInfo<BizAccountFlowVo> list(BizAccountFlowBo bo, PageQuery pageQuery) {
        return bizAccountFlowService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询全部账户明细列表
     */
    @SaCheckPermission("finance:accountFlow:list")
    @GetMapping("/adminPageList")
    public TableDataInfo<BizAccountFlowVo> adminPageList(BizAccountFlowBo bo, PageQuery pageQuery) {
        return bizAccountFlowService.queryAdminPageList(bo, pageQuery);
    }

    /**
     * 导出账户明细列表
     */
    @SaCheckPermission("finance:accountFlow:export")
    @Log(title = "账户明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BizAccountFlowBo bo, HttpServletResponse response) {
        List<BizAccountFlowVo> list = bizAccountFlowService.queryList(bo);
        ExcelUtil.exportExcel(list, "账户明细", BizAccountFlowVo.class, response);
    }

    /**
     * 获取账户明细详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("finance:accountFlow:query")
    @GetMapping("/{id}")
    public R<BizAccountFlowVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(bizAccountFlowService.queryById(id));
    }

    /**
     * 新增账户明细
     */
    @SaCheckPermission("finance:accountFlow:add")
    @Log(title = "账户明细", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BizAccountFlowBo bo) {
        return toAjax(bizAccountFlowService.insertByBo(bo));
    }

    /**
     * 修改账户明细
     */
    @SaCheckPermission("finance:accountFlow:edit")
    @Log(title = "账户明细", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BizAccountFlowBo bo) {
        return toAjax(bizAccountFlowService.updateByBo(bo));
    }

    /**
     * 删除账户明细
     *
     * @param ids 主键串
     */
    @SaCheckPermission("finance:accountFlow:remove")
    @Log(title = "账户明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(bizAccountFlowService.deleteWithValidByIds(List.of(ids), true));
    }

    /**
     * 管理员手动调整账户余额
     */
    //@SaCheckPermission("finance:accountFlow:adjust") // 建议为这个危险操作配一个专属权限字符
    @SaCheckLogin
    @RepeatSubmit(message = "调账请求处理中，请勿重复点击")
    @PostMapping("/adjustBalance")
    public R<Void> adjustBalance(@Validated @RequestBody AccountAdjustReqDTO reqDTO) {
        // 调用 Service 层核心逻辑
        bizUserAccountService.adjustBalance(reqDTO);

        return R.ok("账户余额调账成功");
    }


    @SaCheckLogin
    @PostMapping("/getUserAccountFlow")
    public R<List<BizAccountFlowVo>> getUserAccountFlow(@RequestBody BizAccountFlowBo bo){
        return R.ok(bizAccountFlowService.queryUserAccountFlow(bo));
    }
}
