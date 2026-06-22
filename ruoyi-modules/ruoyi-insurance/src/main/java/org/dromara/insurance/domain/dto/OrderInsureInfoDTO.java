package org.dromara.insurance.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OrderInsureInfoDTO {
    /**
     * 产品模式：3 为卡密产品
     */
    private Integer productMode;

    @NotBlank(message = "起保日期不能为空")
    private String policyStartDate;

    @NotBlank(message = "投保人姓名不能为空")
    private String applicantName;

    @NotBlank(message = "请选择投保人证件类型")
    private String applicantCertType;

    @NotBlank(message = "投保人证件号码不能为空")
    private String applicantCertNo;

    @NotBlank(message = "投保人证件生效起期不能为空")
    private String certStartDate;

    @NotBlank(message = "投保人证件生效止期不能为空")
    private String certEndDate;

    @NotBlank(message = "投保人手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "投保人手机号格式不正确")
    private String applicantPhone;

    @NotBlank(message = "投保人地址不能为空")
    private String applicantAddress;

    // 🌟 核心：使用 List 接收，并要求至少有一名被保人
    @NotEmpty(message = "至少需要填写一名被保人信息")
    @Valid // 🚀 极度重要：开启级联校验，这样 Spring 才会去校验 InsuredItemDTO 里面的 @NotBlank
    private List<OrderInsuredItemDTO> insuredList;

    /**
     * 卡密订单：购买时选择的保险公司
     */
    private String selectedCompanyCode;

    /**
     * 卡密订单：收货人姓名
     */
    private String receiverName;

    /**
     * 卡密订单：收货人手机号
     */
    private String receiverMobile;

    /**
     * 卡密订单：收货地址
     */
    private String receiverAddress;

    /**
     * 产品扩展投保字段值
     */
    private Map<String, Object> extraData;
}
