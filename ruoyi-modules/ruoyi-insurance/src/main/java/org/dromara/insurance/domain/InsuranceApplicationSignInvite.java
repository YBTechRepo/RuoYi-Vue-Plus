package org.dromara.insurance.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/** 投保单公共签署邀请；只保存令牌、会话和证件号摘要。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_insurance_application_sign_invite")
public class InsuranceApplicationSignInvite extends TenantEntity {
    @TableId
    private Long id;
    private String batchOrderNo;
    private String orderNo;
    private Long documentId;
    private String signerRole;
    private String signerName;
    private String slotKeys;
    private String identitySalt;
    private String identityHash;
    private String tokenHash;
    private Date tokenExpiresAt;
    private String sessionHash;
    private Date sessionExpiresAt;
    private String status;
    private Integer failedAttempts;
    private Date lockedUntil;
    private Date verifiedTime;
    private Date signedTime;
    private String statementHash;
    private String requestIp;
    private String userAgent;
}
