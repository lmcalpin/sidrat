package com.sidrat.event.tracking;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import com.sidrat.SidratProcessingException;

public class TrackedObject implements Serializable {
    private static final long serialVersionUID = -6154638687577620218L;

    private String className;
    private String value;
    private Long uniqueID;

    public TrackedObject(Object value, Long uniqueID) {
        this(value.getClass().getName(), stringify(value), uniqueID);
    }

    public TrackedObject(String className, String value, Long uniqueID) {
        this.className = className;
        this.value = value;
        this.uniqueID = uniqueID;
    }

    private static String stringify(Object obj) {
        if (obj != null && obj instanceof Object[]) {
            return Arrays.deepToString((Object[]) obj);
        } else if (obj != null && obj instanceof int[]) {
            return Arrays.toString((int[]) obj);
        } else if (obj != null && obj instanceof short[]) {
            return Arrays.toString((short[]) obj);
        } else if (obj != null && obj instanceof byte[]) {
            return Arrays.toString((byte[]) obj);
        } else if (obj != null && obj instanceof boolean[]) {
            return Arrays.toString((boolean[]) obj);
        } else if (obj != null && obj instanceof long[]) {
            return Arrays.toString((long[]) obj);
        } else if (obj != null && obj instanceof float[]) {
            return Arrays.toString((float[]) obj);
        } else if (obj != null && obj instanceof double[]) {
            return Arrays.toString((double[]) obj);
        }
        return String.valueOf(obj);
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this, obj);
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
    public int hashCode() {
        return Objects.hash(uniqueID, className, value);
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
}
