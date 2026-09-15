package org.dromara.insurance.domain.bo;

import lombok.Data;

/**
 * 渠道回调产品编码映射业务对象
 */
@Data
public class InsuranceProductChannelMappingBo {

    private String companyType;

    private String sourceProductCode;

    private String sourceProductName;
}
