package org.dromara.insurance.controller;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckLogin;
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
import org.dromara.insurance.domain.vo.BannerVo;
import org.dromara.insurance.domain.bo.BannerBo;
import org.dromara.insurance.service.IBannerService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 轮播图配置
 *
 * @author li.xiang
 * @date 2026-03-31
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/insurance/bannerConfig")
public class BannerController extends BaseController {

    private final IBannerService bannerService;

    /**
     * 查询轮播图配置列表
     */
    @SaCheckPermission("insurance:bannerConfig:list")
    @GetMapping("/list")
    public TableDataInfo<BannerVo> list(BannerBo bo, PageQuery pageQuery) {
        return bannerService.queryPageList(bo, pageQuery);
    }

    @SaCheckLogin
    @GetMapping("/salesBannerList")
    public R<List<BannerVo>> salesBannerList() {
        return R.ok(bannerService.querySalesList());
    }

    /**
     * 导出轮播图配置列表
     */
    @SaCheckPermission("insurance:bannerConfig:export")
    @Log(title = "轮播图配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BannerBo bo, HttpServletResponse response) {
        List<BannerVo> list = bannerService.queryList(bo);
        ExcelUtil.exportExcel(list, "轮播图配置", BannerVo.class, response);
    }

    /**
     * 获取轮播图配置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("insurance:bannerConfig:query")
    @GetMapping("/{id}")
    public R<BannerVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(bannerService.queryById(id));
    }

    /**
     * 新增轮播图配置
     */
    @SaCheckPermission("insurance:bannerConfig:add")
    @Log(title = "轮播图配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BannerBo bo) {
        return toAjax(bannerService.insertByBo(bo));
    }

    /**
     * 修改轮播图配置
     */
    @SaCheckPermission("insurance:bannerConfig:edit")
    @Log(title = "轮播图配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BannerBo bo) {
        return toAjax(bannerService.updateByBo(bo));
    }

    /**
     * 删除轮播图配置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("insurance:bannerConfig:remove")
    @Log(title = "轮播图配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(bannerService.deleteWithValidByIds(List.of(ids), true));
    }
}
