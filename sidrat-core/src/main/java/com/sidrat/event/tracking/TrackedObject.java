package com.sidrat.event.tracking;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sidrat.util.Objects;

public class TrackedObject {
    private Object value;
    private Long uniqueID;
    
    public TrackedObject(Object value, Long uniqueID) {
        this.value = value;
        this.uniqueID = uniqueID;
    }
    
    public Object getValue() {
        return value;
    }
    public Long getUniqueID() {
        return uniqueID;
    }
    
}
