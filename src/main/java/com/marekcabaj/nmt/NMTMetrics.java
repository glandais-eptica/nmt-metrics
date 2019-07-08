package com.marekcabaj.nmt;

import static com.marekcabaj.nmt.NMTExtractor.COMMITTED_PROPERTY;
import static com.marekcabaj.nmt.NMTExtractor.RESERVED_PROPERTY;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.BaseUnits;

public class NMTMetrics {

    private final Logger logger = LoggerFactory.getLogger(NMTMetrics.class);

    private MeterRegistry meterRegistry;

    private Map<String, List<Meter>> meters;

    private JcmdCommandRunner jcmdCommandRunner;

    private LoadingCache<String, NMTExtractor> cache;

    public NMTMetrics() {
        this(Metrics.globalRegistry, Duration.ofSeconds(10L));
    }

    public NMTMetrics(MeterRegistry meterRegistry, Duration cacheDuration) {
        super();
        this.meterRegistry = meterRegistry;
        this.meters = Collections.synchronizedMap(new HashMap<>());
        this.jcmdCommandRunner = new JcmdCommandRunner();
        this.cache = Caffeine.newBuilder().expireAfterWrite(cacheDuration).build(this::execute);
        // first call for init
        this.cache.get("");
    }

    protected NMTExtractor execute(String k) {
        NMTExtractor result = new NMTExtractor(this.jcmdCommandRunner.runNMTSummary());
        updateMeters(result);
        return result;
    }

    protected long getValue(String category, String property) {
        return Optional.ofNullable(cache.get("")).map(NMTExtractor::getNMTProperties).map(map -> map.get(category))
                .map(map -> map.get(property)).map(kb -> 1024 * kb).orElse(-1L);
    }

    protected void updateMeters(NMTExtractor result) {
        logger.info("Adding NMT metrics to metrics");
        Set<String> newCategories = Optional.ofNullable(result).map(NMTExtractor::getNMTProperties).map(Map::keySet)
                .orElse(Collections.emptySet());
        Set<String> toRemove = new HashSet<>(meters.keySet());

        Set<String> toAdd = new HashSet<>(newCategories);
        toAdd.removeAll(toRemove);

        toRemove.removeAll(newCategories);
        toRemove.forEach(category -> {
            List<Meter> categoryMeters = meters.remove(category);
            if (categoryMeters != null) {
                categoryMeters.forEach(Metrics.globalRegistry::remove);
            }
        });

        toAdd.forEach(category -> {
            meters.put(category, addMeters(category));
        });
    }

    protected List<Meter> addMeters(String category) {
        List<Meter> list = new ArrayList<>();
        list.add(addMeter(category, RESERVED_PROPERTY, "max possible usage"));
        list.add(addMeter(category, COMMITTED_PROPERTY, "real memory used"));
        return list;
    }

    protected Gauge addMeter(String category, String property, String comment) {
        return Gauge.builder("jvm_memory_nmt_" + property, () -> getValue(category, property)).tag("category", category)
                .description("Native Memory Tracking of the Java virtual machine - " + property + " : " + comment)
                .baseUnit(BaseUnits.BYTES).register(this.meterRegistry);
    }
}
