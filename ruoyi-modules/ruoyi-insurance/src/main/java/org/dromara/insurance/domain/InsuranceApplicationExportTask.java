package org.dromara.insurance.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance_application_export_task")
public class InsuranceApplicationExportTask extends TenantEntity {
    @TableId
    private Long id;
    private Long activeUserId;
    private String requestId;
    private String scope;
    private String criteriaJson;
    private String targetIdsJson;
    private String resultJson;
    private String status;
    private Integer totalCount;
    private Integer successCount;
    private Integer skippedCount;
    private String fileName;
    private String zipKey;
    private String zipHash;
    private Long zipSize;
    private String storageConfig;
    private Date expiresAt;
    private String workerToken;
    private Date heartbeatTime;
    private Date startedAt;
    private Date finishedAt;
    private String failureReason;
}
