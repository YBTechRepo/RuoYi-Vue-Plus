package org.dromara.insurance.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class PolicyCallbackDto {
    private PolicyDto policyDto;
    private AppntDto appntDto;
    private List<InsuredDto> insuredDtoList;
}
