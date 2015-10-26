package com.sidrat.event.tracking;

public class CapturedFieldValue implements ValueTracker {
    private Long time;
    private String ownerID;
    private TrackedObject currentValue;

    public CapturedFieldValue(Long time, String ownerID, TrackedObject value) {
        this.time = time;
        this.ownerID = ownerID;
        this.currentValue = value;
    }

    @Override
    public TrackedObject getCurrentValue() {
        return currentValue;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public Long getTime() {
        return time;
    }
}
