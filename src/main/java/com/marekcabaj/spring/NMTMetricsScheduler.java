package com.marekcabaj.spring;

import org.springframework.scheduling.annotation.Scheduled;

import com.marekcabaj.nmt.NMTMetrics;

public class NMTMetricsScheduler {

    private NMTMetrics nmtMetrics;

    public NMTMetricsScheduler(NMTMetrics nmtMetrics) {
        super();
        this.nmtMetrics = nmtMetrics;
    }

    @Scheduled(fixedRate = 1000, initialDelay = 0)
    protected synchronized void update() {
        this.nmtMetrics.updateMeters();
    }

}
