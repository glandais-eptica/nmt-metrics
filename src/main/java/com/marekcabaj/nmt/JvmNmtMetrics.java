package com.marekcabaj.nmt;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.marekcabaj.nmt.bean.NativeMemoryTrackingKind;
import com.marekcabaj.nmt.bean.NativeMemoryTrackingType;
import com.marekcabaj.nmt.bean.NativeMemoryTrackingValues;
import com.marekcabaj.nmt.jcmd.NMTJcmdRetriever;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.binder.MeterBinder;

public class JvmNmtMetrics implements MeterBinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmNmtMetrics.class);

    private MeterRegistry meterRegistry;

    private Map<NativeMemoryTrackingType, List<Meter>> meters;

    private NMTJcmdRetriever nmtJcmdRetriever;

    private LoadingCache<String, NativeMemoryTrackingValues> nativeMemoryTrackingValuesCache;

    public JvmNmtMetrics() {
        this(Duration.ofSeconds(10L));
    }

    public JvmNmtMetrics(Duration cacheDuration) {
        super();
        this.meters = Collections
                .synchronizedMap(new EnumMap<NativeMemoryTrackingType, List<Meter>>(NativeMemoryTrackingType.class));
        this.nmtJcmdRetriever = new NMTJcmdRetriever();
        this.nativeMemoryTrackingValuesCache = Caffeine.newBuilder().expireAfterWrite(cacheDuration)
                .build(this::computeVmNativeMemorySummary);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;

        // first call for init
        NativeMemoryTrackingValues initialSummary = this.getVmNativeMemorySummary();
        LOGGER.debug("Initial summary : {}", initialSummary);
    }

    protected NativeMemoryTrackingValues getVmNativeMemorySummary() {
        return this.nativeMemoryTrackingValuesCache.get(NMTJcmdRetriever.VM_NATIVE_MEMORY_SUMMARY_COMMAND);
    }

    protected NativeMemoryTrackingValues computeVmNativeMemorySummary(String command) {
        NativeMemoryTrackingValues result = this.nmtJcmdRetriever.retrieveNativeMemoryTrackingValues(command);
        updateMeters(result);
        return result;
    }

    protected void updateMeters(NativeMemoryTrackingValues result) {
        LOGGER.debug("Adding NMT metrics to Micrometer");

        Set<NativeMemoryTrackingType> categories = result.values().stream().flatMap(m -> m.keySet().stream())
                .collect(Collectors.toSet());

        LOGGER.debug("NMT metric categories : {}", categories);

        Set<NativeMemoryTrackingType> toRemove = new HashSet<>(meters.keySet());

        Set<NativeMemoryTrackingType> toAdd = new HashSet<>(categories);
        toAdd.removeAll(toRemove);

        toRemove.removeAll(categories);

        LOGGER.debug("NMT metric categories to remove : {}", toRemove);

        toRemove.forEach(category -> {
            List<Meter> categoryMeters = meters.remove(category);
            if (categoryMeters != null) {
                categoryMeters.forEach(this.meterRegistry::remove);
            }
        });

        LOGGER.debug("NMT metric categories to add : {}", toAdd);

        toAdd.forEach(category -> meters.put(category, addMeters(category)));
    }

    protected List<Meter> addMeters(NativeMemoryTrackingType nmtType) {
        List<Meter> list = new ArrayList<>();
        list.add(addMeter(NativeMemoryTrackingKind.RESERVED, nmtType));
        list.add(addMeter(NativeMemoryTrackingKind.COMMITTED, nmtType));
        return list;
    }

    protected Gauge addMeter(NativeMemoryTrackingKind nmtKind, NativeMemoryTrackingType nmtType) {
        String kindName = nmtKind.name().toLowerCase();
        String description = "Native Memory Tracking of the Java virtual machine - " + kindName + " : "
                + nmtKind.getComment();
        return Gauge.builder("jvm.memory.nmt." + kindName, () -> getValue(nmtKind, nmtType))
                .tag("category", nmtType.getCategory()).description(description).baseUnit(BaseUnits.BYTES)
                .register(this.meterRegistry);
    }

    protected long getValue(NativeMemoryTrackingKind nmtKind, NativeMemoryTrackingType nmtType) {
        return Optional.ofNullable(getVmNativeMemorySummary()).map(map -> map.get(nmtKind)).map(map -> map.get(nmtType))
                .map(kb -> 1024 * kb).orElse(-1L);
    }

}
