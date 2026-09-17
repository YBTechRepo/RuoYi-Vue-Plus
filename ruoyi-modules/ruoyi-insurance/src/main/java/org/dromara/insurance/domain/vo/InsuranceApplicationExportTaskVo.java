package org.dromara.insurance.domain.vo;

import lombok.Data;

import java.util.Date;

@Data
public class InsuranceApplicationExportTaskVo {
    private Long id;
    private String scope;
    private String status;
    private Integer totalCount;
    private Integer successCount;
    private Integer skippedCount;
    private String fileName;
    private Long zipSize;
    private Date expiresAt;
    private Date startedAt;
    private Date finishedAt;
    private String failureReason;
    private Date createTime;
    private Boolean canDownload;
}
