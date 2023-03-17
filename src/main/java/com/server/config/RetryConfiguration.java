package com.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRetry
public class RetryConfiguration {

    @Value("${request.retry.max-attempts:5}")
    private int requestRetryMaxAttempts;

    @Value("${request.retry.backoff:1000}")
    private int requestRetryBackoff;

    @Bean
    public RetryTemplate retryTemplate() {
        return RetryTemplate.builder()
                .maxAttempts(requestRetryMaxAttempts)
                .fixedBackoff(requestRetryBackoff)
                .build();
    }

}