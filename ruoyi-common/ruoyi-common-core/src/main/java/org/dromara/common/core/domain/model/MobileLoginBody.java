package org.dromara.common.core.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;

/**
 * 手机号登录请求参数
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MobileLoginBody extends LoginBody {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 手机号
     */
    @NotBlank(message = "{user.phone.not.blank}")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "{user.phone.format.error}")
    private String phoneNumber;

    /**
     * 密码
     */
    @NotBlank(message = "{user.password.not.blank}")
    @Length(min = 5, max = 30, message = "{user.password.length.valid}")
    private String password;
}
