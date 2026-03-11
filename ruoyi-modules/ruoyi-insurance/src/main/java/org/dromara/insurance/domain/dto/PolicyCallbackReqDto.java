package org.dromara.insurance.domain.dto;

import lombok.Data;

@Data
public class PolicyCallbackReqDto {
    private String content;
    private String sign;
}
