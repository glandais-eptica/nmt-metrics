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
import io.micrometer.core.instrument.binder.BaseUnits;

public class NMTMetrics {

    private final Logger logger = LoggerFactory.getLogger(NMTMetrics.class);

    private MeterRegistry registry;

    private Duration cacheDuration;

    private Map<String, List<Meter>> meters;

    private JcmdCommandRunner jcmdCommandRunner;

    private LoadingCache<String, NMTExtractor> cache;

    public NMTMetrics(MeterRegistry registry, JcmdCommandRunner jcmdCommandRunner, Duration cacheDuration) {
        super();
        this.registry = registry;
        this.cacheDuration = cacheDuration;
        this.meters = Collections.synchronizedMap(new HashMap<>());
        this.jcmdCommandRunner = jcmdCommandRunner;
        this.cache = Caffeine.newBuilder().expireAfterWrite(cacheDuration).build(this::execute);
    }

    public Duration getCacheDuration() {
        return this.cacheDuration;
    }

    protected NMTExtractor execute(String k) {
        return new NMTExtractor(this.jcmdCommandRunner.runNMTSummary());
    }

    public Optional<Map<String, Map<String, Long>>> getNMTProperties() {
        return Optional.ofNullable(cache.get("")).map(NMTExtractor::getNMTProperties);
    }

    public Set<String> getCategories() {
        return getNMTProperties().map(Map::keySet).orElse(Collections.emptySet());
    }

    public long getValue(String category, String property) {
        return getNMTProperties().map(map -> map.get(category)).map(map -> map.get(property)).map(kb -> 1024 * kb).orElse(-1L);
    }

    public synchronized void updateMeters() {
        logger.info("Adding NMT metrics to metrics");
        Set<String> newCategories = new HashSet<>(getCategories());
        Set<String> toRemove = new HashSet<>(meters.keySet());

        Set<String> toAdd = new HashSet<>(newCategories);
        toAdd.removeAll(toRemove);

        toRemove.removeAll(newCategories);
        toRemove.forEach(category -> {
            List<Meter> categoryMeters = meters.remove(category);
            if (categoryMeters != null) {
                categoryMeters.forEach(registry::remove);
            }
        });

        toAdd.forEach(category -> {
            List<Meter> list = new ArrayList<>();
            meters.put(category, list);
            list.add(addMeter(category, RESERVED_PROPERTY, "max possible usage"));
            list.add(addMeter(category, COMMITTED_PROPERTY, "real memory used"));
        });
    }

    private Gauge addMeter(String category, String property, String comment) {
        return Gauge.builder("jvm_memory_nmt_" + property, () -> getValue(category, property))
                .tag("category", category)
                .description("Native Memory Tracking of the Java virtual machine - " + property + " : " + comment)
                .baseUnit(BaseUnits.BYTES)
                .register(registry);
    }
}
