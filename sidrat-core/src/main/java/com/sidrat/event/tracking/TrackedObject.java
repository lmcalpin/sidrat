package com.sidrat.event.tracking;

import java.io.Serializable;
import java.util.Objects;

import com.sidrat.SidratProcessingException;

public class TrackedObject implements Serializable {
    private static final long serialVersionUID = -6154638687577620218L;
    
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
    
    public String getValueAsString() {
        return value;
    }

    @Override
    public String toString() {
        if (className == null) {
            return "null";
        }
        if (uniqueID != null && className != null) {
            return className + "#" + uniqueID;
        }
        throw new SidratProcessingException("TrackedObject corrupt: " + uniqueID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueID, className, value);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this,  obj);
    }
}
