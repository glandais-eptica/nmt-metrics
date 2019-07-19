package com.marekcabaj.nmt.bean;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

// from src/hotspot/share/services/nmtCommon.cpp
public enum NativeMemoryTrackingType {

    // total
    TOTAL("Total"),

    JAVA_HEAP("Java Heap"),

    CLASS("Class"),

    THREAD("Thread"),

    THREAD_STACK("Thread Stack"),

    CODE("Code"),

    GC("GC"),

    COMPILER("Compiler"),

    INTERNAL("Internal"),

    OTHER("Other"),

    SYMBOL("Symbol"),

    NMT("Native Memory Tracking"),

    SHARED_CLASS_SPACE("Shared class space"),

    ARENA_CHUNK("Arena Chunk"),

    TEST("Test"),

    TRACING("Tracing"),

    LOGGING("Logging"),

    ARGUMENTS("Arguments"),

    MODULE("Module"),

    UNKNOWN("Unknown");

    private String label;

    private String category;

    private NativeMemoryTrackingType(String label) {
        this.label = label;
        this.category = label.toLowerCase().replace(' ', '.');
    }

    public String getLabel() {
        return label;
    }

    public String getCategory() {
        return category;
    }

    private static final LoadingCache<String, NativeMemoryTrackingType> BY_LABEL = Caffeine.newBuilder().build(NativeMemoryTrackingType::findByName);

    protected static NativeMemoryTrackingType findByName(String label) {
        for (NativeMemoryTrackingType nmtType : values()) {
            if (nmtType.getLabel().equals(label)) {
                return nmtType;
            }
        }
        throw new IllegalArgumentException("No enum constant for label " + label);
    }

    public static NativeMemoryTrackingType getByLabel(String label) {
        return BY_LABEL.get(label);
    }
}
