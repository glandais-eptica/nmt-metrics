package com.marekcabaj.nmt.bean;

public enum NativeMemoryTrackingKind {

    RESERVED("reserved memory (max possible usage)"),

    COMMITTED("committed memory (real memory used)");

    private String comment;

    private NativeMemoryTrackingKind(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

}
