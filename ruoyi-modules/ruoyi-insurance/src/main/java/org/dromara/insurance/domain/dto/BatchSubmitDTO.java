package org.dromara.insurance.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchSubmitDTO {
    private Long productId;
    private String policyStartDate;
    private List<BatchInsuredImportDto> auditList;
    /** 以下字段仅供需要签字投保单的产品使用。 */
    private String templateMode;
    private String templateVersion;
    private String schemaHash;
    private List<SignedBatchInsuredImportDto> signedAuditList;
}
