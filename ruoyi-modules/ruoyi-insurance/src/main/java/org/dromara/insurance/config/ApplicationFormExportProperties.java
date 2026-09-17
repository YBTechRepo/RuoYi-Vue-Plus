package org.dromara.insurance.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "insurance.application-form.batch-export")
public class ApplicationFormExportProperties {
    private int maxOrders = 200;
    private long maxBytes = 1024L * 1024 * 1024;
    private int retentionHours = 24;
    private int workerConcurrency = 1;
}
