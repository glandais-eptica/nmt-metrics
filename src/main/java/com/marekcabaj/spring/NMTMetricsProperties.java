package com.marekcabaj.spring;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nmt.metrics")
public class NMTMetricsProperties {

    private Duration cacheDuration = Duration.ofSeconds(30L);

    public void setCacheDuration(Duration cacheDuration) {
        this.cacheDuration = cacheDuration;
    }

    public Duration getCacheDuration() {
        return cacheDuration;
    }

}
