package org.dromara.insurance.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchSubmitDTO {
    private Long productId;
    private String policyStartDate;
    private List<BatchInsuredImportDto> auditList;
}
