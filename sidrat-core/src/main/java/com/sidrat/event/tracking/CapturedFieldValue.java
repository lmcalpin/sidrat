package com.sidrat.event.tracking;

public class CapturedFieldValue implements ValueTracker {
    private Long time;
    private Long ownerID;
    private TrackedObject currentValue;
    
    public CapturedFieldValue(Long time, Long ownerID, TrackedObject value) {
        this.time = time;
        this.ownerID = ownerID;
        this.currentValue = value;
    }
    
    public Long getTime() {
        return time;
    }

    public Long getOwnerID() {
        return ownerID;
    }

    public TrackedObject getCurrentValue() {
        return currentValue;
    }
}
