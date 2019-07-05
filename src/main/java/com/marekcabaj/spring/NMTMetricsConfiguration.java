package com.marekcabaj.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import com.marekcabaj.nmt.JcmdCommandRunner;
import com.marekcabaj.nmt.NMTMetrics;

import io.micrometer.core.instrument.MeterRegistry;

@Configuration
@ConditionalOnProperty(value = "nmt.metrics.enabled", matchIfMissing = true)
@EnableConfigurationProperties(NMTMetricsProperties.class)
public class NMTMetricsConfiguration {

    @Bean
    @Lazy(false)
    public NMTMetricsScheduler nmtMetricsScheduler(NMTMetrics nmtMetrics) {
        return new NMTMetricsScheduler(nmtMetrics);
    }

    @Bean
    public NMTMetrics nmtMetrics(MeterRegistry registry, JcmdCommandRunner jcmdCommandRunner,
            NMTMetricsProperties nmtMetricsProperties) {
        return new NMTMetrics(registry, jcmdCommandRunner, nmtMetricsProperties.getCacheDuration());
    }

    @Bean
    public JcmdCommandRunner jcmdCommandRunner(Environment environment) {
        return new JcmdCommandRunner(environment);
    }

}
