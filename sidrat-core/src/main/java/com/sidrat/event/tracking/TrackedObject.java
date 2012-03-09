package com.sidrat.event.tracking;

import com.sidrat.SidratProcessingException;

public class TrackedObject {
    private String className;
    private String value;
    private Long uniqueID;
    
    public TrackedObject(Object value, Long uniqueID) {
        this(value.getClass().getName(), String.valueOf(value), uniqueID);
    }
    
    public TrackedObject(String className, String value, Long uniqueID) {
        this.className = className;
        this.value = value;
        this.uniqueID = uniqueID;
    }
    
    public String getClassName() {
        return className;
    }
    
    public Long getUniqueID() {
        return uniqueID;
    }
    
    public String getValue() {
        return value;
    }

    public String toString() {
        if (className == null) {
            return "null";
        }
        if (uniqueID != null && className != null) {
            return className + "#" + uniqueID;
        }
        throw new SidratProcessingException("TrackedObject corrupt: " + uniqueID);
    }
    
}
