package com.sidrat.event.tracking;

import java.io.Serializable;

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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((uniqueID == null) ? 0 : uniqueID.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TrackedObject other = (TrackedObject) obj;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (uniqueID == null) {
            if (other.uniqueID != null)
                return false;
        } else if (!uniqueID.equals(other.uniqueID))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
}
