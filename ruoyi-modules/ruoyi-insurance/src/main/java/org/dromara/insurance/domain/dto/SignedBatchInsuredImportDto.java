package org.dromara.insurance.domain.dto;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 需要签字投保单产品的批量导入行。
 *
 * <p>该模型独立于 {@link BatchInsuredImportDto}，避免签字产品新增字段改变普通批量投保的校验规则。</p>
 */
@Data
public class SignedBatchInsuredImportDto {
    private String appName;
    private String appCertType;
    private String appCertNo;
    private String appCertStartDate;
    private String appCertEndDate;
    private String appPhone;
    private String appRegion;
    private String appAddress;

    private String relation;
    private String name;
    private String certType;
    private String certNo;
    private String certStartDate;
    private String certEndDate;
    private String phone;
    private String region;
    private String address;

    private Boolean isValid;
    private Boolean isEditing = false;
    private Map<String, String> errors = new LinkedHashMap<>();
    /** 产品扩展字段，按现有单笔投保约定保存在 extraData 顶层。 */
    private Map<String, Object> extraData = new LinkedHashMap<>();
    /** 投保单专属可填写字段，提交时写入 extraData.applicationForm。 */
    private Map<String, Object> applicationForm = new LinkedHashMap<>();
}
