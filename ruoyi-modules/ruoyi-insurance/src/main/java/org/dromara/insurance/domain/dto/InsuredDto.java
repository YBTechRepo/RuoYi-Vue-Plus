package org.dromara.insurance.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class InsuredDto {
    private String relationToAppnt;
    private String name;
    private String sex;
    private String idType;
    private String idNo;
    private String mobile;
    private String email;
    private String birthday;
    private String homeAddress;
    private List<RiskDto> riskList;
}
