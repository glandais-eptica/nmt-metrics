package com.marekcabaj.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.marekcabaj.nmt.NMTMetrics;

@Configuration
@ConditionalOnProperty(value = "nmt.metrics.enabled", matchIfMissing = true)
public class NMTMetricsConfiguration {

    @Bean
    @Lazy(false)
    public NMTMetrics nmtMetrics() {
        return new NMTMetrics();
    }

}
