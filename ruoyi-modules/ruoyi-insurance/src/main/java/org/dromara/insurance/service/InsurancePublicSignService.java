package org.dromara.insurance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.insurance.domain.InsuranceApplicationDocument;
import org.dromara.insurance.domain.InsuranceApplicationSignInvite;
import org.dromara.insurance.domain.InsuranceApplyRecord;
import org.dromara.insurance.domain.InsuranceOrderApplicant;
import org.dromara.insurance.domain.InsuranceOrderInsured;
import org.dromara.insurance.mapper.InsuranceApplicationSignInviteMapper;
import org.dromara.insurance.mapper.InsuranceApplicationDocumentMapper;
import org.dromara.insurance.mapper.InsuranceOrderApplicantMapper;
import org.dromara.insurance.mapper.InsuranceOrderInsuredMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 免登录签署邀请和分栏签署。 */
@Service
@RequiredArgsConstructor
public class InsurancePublicSignService {
    private final InsuranceApplicationSignInviteMapper invites;
    private final InsuranceApplicationDocumentMapper documents;
    private final InsuranceOrderApplicantMapper applicants;
    private final InsuranceOrderInsuredMapper insureds;
    private final InsuranceApplicationFormService applicationFormService;
    private final ObjectMapper json;

    @Value("${insurance.application-form.public-sign-url:}")
    private String publicSignUrl;

    @Transactional(rollbackFor = Exception.class)
    public void createInvites(InsuranceApplyRecord order, InsuranceApplicationDocument document,
                              InsuranceOrderApplicant applicant, InsuranceOrderInsured insured) {
        if (order == null || document == null || applicant == null || insured == null) {
            throw new ServiceException("签署邀请资料不完整");
        }
        JsonNode snapshot;
        try {
            snapshot = json.readTree(document.getSnapshotJson());
        } catch (Exception e) {
            throw new ServiceException("投保单快照无法生成签署邀请");
        }
        boolean minor = "法定监护人".equals(snapshot.path("insuredSignerRole").asText());
        boolean samePerson = Objects.equals(applicant.getApplicantCertNo(), insured.getInsuredCertNo());
        if (minor || samePerson) {
            create(order, document, minor ? "GUARDIAN" : "APPLICANT_INSURED", applicant.getApplicantName(),
                applicant.getApplicantCertNo(), List.of("special", "applicant", "insured"));
        } else {
            create(order, document, "APPLICANT", applicant.getApplicantName(), applicant.getApplicantCertNo(),
                List.of("special", "applicant"));
            create(order, document, "INSURED", insured.getInsuredName(), insured.getInsuredCertNo(),
                List.of("insured"));
        }
    }

    public List<Map<String, Object>> list(String batchOrderNo, String orderNo) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (InsuranceApplicationSignInvite invite : invites.selectList(
            new LambdaQueryWrapper<InsuranceApplicationSignInvite>()
                .eq(InsuranceApplicationSignInvite::getBatchOrderNo, batchOrderNo)
                .eq(InsuranceApplicationSignInvite::getOrderNo, orderNo)
                .orderByAsc(InsuranceApplicationSignInvite::getId))) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("inviteId", invite.getId().toString());
            row.put("signerRole", invite.getSignerRole());
            row.put("signerName", invite.getSignerName());
            row.put("slotKeys", slots(invite));
            row.put("status", invite.getStatus());
            row.put("tokenExpiresAt", invite.getTokenExpiresAt());
            row.put("lockedUntil", invite.getLockedUntil());
            result.add(row);
        }
        return result;
    }

    public InsuranceApplicationSignInvite findOwnedInvite(String batchOrderNo, Long inviteId) {
        InsuranceApplicationSignInvite invite = invites.selectOne(
            new LambdaQueryWrapper<InsuranceApplicationSignInvite>()
                .eq(InsuranceApplicationSignInvite::getId, inviteId)
                .eq(InsuranceApplicationSignInvite::getBatchOrderNo, batchOrderNo));
        if (invite == null) throw new ServiceException("签署邀请不存在");
        return invite;
    }

    public List<InsuranceApplicationSignInvite> findOwnedInvites(String batchOrderNo) {
        return invites.selectList(new LambdaQueryWrapper<InsuranceApplicationSignInvite>()
            .eq(InsuranceApplicationSignInvite::getBatchOrderNo, batchOrderNo)
            .orderByAsc(InsuranceApplicationSignInvite::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> issueLink(Long inviteId, InsuranceApplyRecord child) {
        if (StringUtils.isBlank(publicSignUrl)) {
            throw new ServiceException("请配置公共签署地址 insurance.application-form.public-sign-url");
        }
        InsuranceApplicationSignInvite invite = invites.selectById(inviteId);
        if (invite == null || !Objects.equals(invite.getOrderNo(), child.getOrderNo())) {
            throw new ServiceException("签署邀请不存在");
        }
        if ("SIGNED".equals(invite.getStatus())) throw new ServiceException("本签署任务已完成，不能重新生成链接");
        Instant now = Instant.now();
        Instant policyStart = child.getPolicyStartDate().toInstant();
        Instant expiresAt = now.plus(7, ChronoUnit.DAYS).isBefore(policyStart)
            ? now.plus(7, ChronoUnit.DAYS) : policyStart;
        if (!expiresAt.isAfter(now)) throw new ServiceException("起保日期已到，不能再生成签署链接");
        String token = randomToken();
        invite.setTokenHash(hash(token));
        invite.setTokenExpiresAt(Date.from(expiresAt));
        invite.setSessionHash(null);
        invite.setSessionExpiresAt(null);
        invite.setFailedAttempts(0);
        invite.setLockedUntil(null);
        invite.setStatus("PENDING");
        invites.updateById(invite);
        String separator = publicSignUrl.contains("?") ? "&" : "?";
        return Map.of("inviteId", invite.getId().toString(), "url", publicSignUrl + separator + "token=" + token,
            "expiresAt", invite.getTokenExpiresAt());
    }

    public Map<String, Object> resolve(String token) {
        InsuranceApplicationSignInvite invite = byToken(token);
        assertTokenUsable(invite);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("signerRole", invite.getSignerRole());
        result.put("signerName", invite.getSignerName());
        result.put("signerCertificateType", signerCertificateType(invite));
        result.put("slotKeys", slots(invite));
        result.put("status", invite.getStatus());
        result.put("expiresAt", invite.getTokenExpiresAt());
        result.put("statements", statements(invite));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> sign(String token, Map<String, byte[]> images, String ip, String ua) {
        InsuranceApplicationSignInvite invite = byToken(token);
        assertTokenUsable(invite);
        List<String> slotKeys = slots(invite);
        try {
            Map<String, Object> result = TenantHelper.dynamic(invite.getTenantId(), () -> {
                try {
                    return applicationFormService.signPublic(invite.getOrderNo(), invite.getDocumentId(), slotKeys, images, ip, ua);
                } catch (java.io.IOException e) {
                    throw new ServiceException("签字图片处理失败");
                }
            });
            invite.setSignedTime(new Date());
            invite.setStatus("SIGNED");
            invite.setStatementHash(statementHash(invite));
            invite.setRequestIp(ip);
            invite.setUserAgent(trimUa(ua));
            updateIgnored(invite);
            return result;
        } catch (ServiceException e) {
            throw e;
        }
    }

    private void create(InsuranceApplyRecord order, InsuranceApplicationDocument document, String role,
                        String name, String certificateNo, List<String> slotKeys) {
        InsuranceApplicationSignInvite invite = new InsuranceApplicationSignInvite();
        invite.setBatchOrderNo(order.getBatchOrderNo());
        invite.setOrderNo(order.getOrderNo());
        invite.setDocumentId(document.getId());
        invite.setSignerRole(role);
        invite.setSignerName(name);
        invite.setSlotKeys(String.join(",", slotKeys));
        invite.setIdentitySalt(randomToken());
        invite.setIdentityHash(identityHash(invite.getIdentitySalt(), certificateNo));
        invite.setStatus("CREATED");
        invite.setFailedAttempts(0);
        invites.insert(invite);
    }

    private InsuranceApplicationSignInvite byToken(String token) {
        if (StringUtils.isBlank(token)) throw new ServiceException("签署令牌不能为空");
        InsuranceApplicationSignInvite invite = TenantHelper.ignore(() -> invites.selectOne(
            new LambdaQueryWrapper<InsuranceApplicationSignInvite>()
                .eq(InsuranceApplicationSignInvite::getTokenHash, hash(token)).last("LIMIT 1")));
        if (invite == null) throw new ServiceException("签署链接无效");
        return invite;
    }

    private void assertTokenUsable(InsuranceApplicationSignInvite invite) {
        Date now = new Date();
        if (invite.getTokenExpiresAt() == null || !invite.getTokenExpiresAt().after(now)) throw new ServiceException("签署链接已过期");
        if ("SIGNED".equals(invite.getStatus())) throw new ServiceException("本签署任务已完成");
    }

    private void updateIgnored(InsuranceApplicationSignInvite invite) {
        TenantHelper.ignore(() -> invites.updateById(invite));
    }

    private List<String> slots(InsuranceApplicationSignInvite invite) {
        return Arrays.stream(invite.getSlotKeys().split(",")).filter(StringUtils::isNotBlank).toList();
    }

    private String identityHash(String salt, String certificateNo) {
        return hash(salt + ":" + StringUtils.trimToEmpty(certificateNo).toUpperCase());
    }

    private String hash(String value) {
        return ApplicationFormTemplate.hash(value.getBytes(StandardCharsets.UTF_8));
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String signerCertificateType(InsuranceApplicationSignInvite invite) {
        return TenantHelper.dynamic(invite.getTenantId(), () -> {
            if ("INSURED".equals(invite.getSignerRole())) {
                InsuranceOrderInsured insured = insureds.selectOne(new LambdaQueryWrapper<InsuranceOrderInsured>()
                    .eq(InsuranceOrderInsured::getOrderNo, invite.getOrderNo()).last("LIMIT 1"));
                return insured == null ? "" : insured.getInsuredCertType();
            }
            InsuranceOrderApplicant applicant = applicants.selectOne(new LambdaQueryWrapper<InsuranceOrderApplicant>()
                .eq(InsuranceOrderApplicant::getOrderNo, invite.getOrderNo()).last("LIMIT 1"));
            return applicant == null ? "" : applicant.getApplicantCertType();
        });
    }

    private String trimUa(String ua) {
        return ua == null ? "" : ua.substring(0, Math.min(ua.length(), 500));
    }

    private List<Map<String, Object>> statements(InsuranceApplicationSignInvite invite) {
        InsuranceApplicationDocument document = TenantHelper.ignore(() -> documents.selectById(invite.getDocumentId()));
        if (document == null) throw new ServiceException("投保单签署版本不存在");
        JsonNode metadata = applicationFormService.metadata(document.getTemplateCode(), document.getTemplateVersion());
        List<String> allowed = slots(invite);
        List<Map<String, Object>> result = new ArrayList<>();
        for (JsonNode statement : metadata.path("statements")) {
            String key = statement.path("key").asText();
            if (!allowed.contains(key)) continue;
            List<String> content = new ArrayList<>();
            for (JsonNode paragraph : statement.path("content")) content.add(paragraph.asText());
            result.add(Map.of("key", key, "title", statement.path("title").asText(), "content", content));
        }
        return result;
    }

    private String statementHash(InsuranceApplicationSignInvite invite) {
        try {
            return ApplicationFormTemplate.hash(json.writeValueAsBytes(Map.of(
                "documentId", invite.getDocumentId().toString(),
                "role", invite.getSignerRole(),
                "slots", slots(invite),
                "statements", statements(invite))));
        } catch (Exception e) {
            throw new ServiceException("签署声明摘要计算失败");
        }
    }
}
