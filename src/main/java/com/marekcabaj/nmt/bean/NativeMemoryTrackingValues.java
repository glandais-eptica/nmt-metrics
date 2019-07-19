package com.marekcabaj.nmt.bean;

import java.util.EnumMap;
import java.util.Map;

public class NativeMemoryTrackingValues extends EnumMap<NativeMemoryTrackingKind, Map<NativeMemoryTrackingType, Long>> {

    private static final long serialVersionUID = 5607550568043045484L;

    public NativeMemoryTrackingValues() {
        super(NativeMemoryTrackingKind.class);
    }

}
