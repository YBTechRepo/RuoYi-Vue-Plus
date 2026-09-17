package org.dromara.insurance.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance_application_document")
public class InsuranceApplicationDocument extends TenantEntity {
    @TableId private Long id;
    private String orderNo;
    private String templateCode;
    private String templateVersion;
    private String templateHash;
    private String snapshotJson;
    private String snapshotHash;
    /** DRAFT / GENERATING / READY / FAILED / INVALID */
    private String status;
    private String signatureJson;
    private Date signedTime;
    private String requestIp;
    private String userAgent;
    private String pdfKey;
    private String pdfHash;
    private String storageConfig;
    private String failureReason;
    private String generationToken;
}
