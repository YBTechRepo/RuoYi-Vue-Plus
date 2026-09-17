package org.dromara.insurance.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@RequiredArgsConstructor
public class ApplicationFormExportConfig {
    private final ApplicationFormExportProperties properties;

    @Bean("applicationFormExportExecutor")
    public ThreadPoolTaskExecutor applicationFormExportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int concurrency = Math.max(1, properties.getWorkerConcurrency());
        executor.setCorePoolSize(concurrency);
        executor.setMaxPoolSize(concurrency);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("application-form-export-");
        executor.initialize();
        return executor;
    }
}
