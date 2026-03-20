package org.dromara.insurance.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MarketProductVo extends InsuranceProductConfigVo{
    private Boolean hasAdded;

    /**
     * 🌟 终极防御：重写父类字段，干掉 @Translation 注解！
     * 防止 Jackson 序列化时再次触发自动翻译，把咱们手动查出来的真实链接给覆盖成空！
     */
    private String imgUrlUrl;
}
