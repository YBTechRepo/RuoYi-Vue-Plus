package org.dromara.insurance.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class InsuranceApplicationExportRequest {
    @NotBlank(message = "请求标识不能为空")
    @Size(max = 64, message = "请求标识长度不能超过64")
    private String requestId;

    @NotBlank(message = "导出范围不能为空")
    @Pattern(regexp = "SELECTED|FILTER", message = "导出范围无效")
    private String scope;

    @Size(max = 200, message = "单次最多选择200个订单")
    private List<Long> orderIds;

    @Valid
    private InsuranceApplyRecordBo query;
}
