package org.dromara.insurance.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "insurance.openapi")
@NoArgsConstructor
@AllArgsConstructor
public class OpenApiProperties {
    private String publicKey;
    private String privateKey;
    private String appCode;
}
