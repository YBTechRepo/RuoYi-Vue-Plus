package org.dromara.insurance.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OrderInsuredItemDTO {
    @NotBlank(message = "请选择与投保人的关系")
    private String relation;

    @NotBlank(message = "被保人姓名不能为空")
    private String insuredName;

    @NotBlank(message = "请选择被保人证件类型")
    private String insuredCertType;

    @NotBlank(message = "被保人证件号码不能为空")
    // 🛑 移除了写死的身份证正则，兼容护照/出生医学证明等
    private String insuredCertNo;

    @NotBlank(message = "被保人证件生效起期不能为空")
    private String certStartDate;

    @NotBlank(message = "被保人证件生效止期不能为空")
    private String certEndDate;

    // 💡 提示：如果是给未成年人投保，手机号往往不是必填项。
    // 如果您的业务强制要求被保人留电话，可以保留 @NotBlank 和正则；如果允许为空，请删掉这两个注解。
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "被保人手机号格式不正确")
    private String insuredPhone;

    @NotBlank(message = "被保人地址不能为空")
    private String insuredAddress;
}
