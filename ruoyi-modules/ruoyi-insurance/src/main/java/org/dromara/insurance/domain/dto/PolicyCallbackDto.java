package org.dromara.insurance.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class PolicyCallbackDto {
    private PolicyDto policy;
    private AppntDto appnt;
    private List<InsuredDto> insureds;
}
