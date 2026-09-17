package org.dromara.insurance.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.TenantConstants;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.insurance.domain.bo.InsuranceApplicationExportRequest;
import org.dromara.insurance.domain.vo.InsuranceApplicationExportTaskVo;
import org.dromara.insurance.service.InsuranceApplicationExportService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Validated
@RestController
@RequiredArgsConstructor
@SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
@SaCheckPermission("insurance:insuranceProxyOrder:query")
@RequestMapping("/insurance/insuranceProxyOrder/applicationForm/exportTasks")
public class InsuranceApplicationExportController {
    private final InsuranceApplicationExportService service;

    @Log(title = "批量导出投保单", businessType = BusinessType.EXPORT, isSaveRequestData = false)
    @PostMapping
    public R<InsuranceApplicationExportTaskVo> create(@Valid @RequestBody InsuranceApplicationExportRequest request) {
        return R.ok(service.create(request));
    }

    @GetMapping
    public TableDataInfo<InsuranceApplicationExportTaskVo> list(PageQuery pageQuery) {
        return service.list(pageQuery);
    }

    @GetMapping("/{taskId}")
    public R<InsuranceApplicationExportTaskVo> get(@NotNull @PathVariable Long taskId) {
        return R.ok(service.get(taskId));
    }

    @Log(title = "下载批量投保单", businessType = BusinessType.EXPORT)
    @GetMapping("/{taskId}/file")
    public void download(@NotNull @PathVariable Long taskId, HttpServletResponse response) throws IOException {
        InsuranceApplicationExportService.ExportFile file = service.file(taskId);
        response.setContentType("application/zip");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" +
            URLEncoder.encode(file.fileName(), StandardCharsets.UTF_8).replace("+", "%20"));
        response.setContentLengthLong(file.size());
        service.write(file, response.getOutputStream());
    }
}
