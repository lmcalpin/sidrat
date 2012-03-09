package com.sidrat.event.tracking;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import com.sidrat.SidratDebugger;
import com.sidrat.util.Objects;

public class TrackedObjects {
    private Map<Long, WeakReference<TrackedObject>> trackedObjects = new HashMap<Long, WeakReference<TrackedObject>>();
    
    private Long getUniqueIDFor(Object obj) {
        Long id = Objects.getUniqueIdentifier(obj);
        if (!trackedObjects.containsKey(id))
            trackedObjects.put(id, new WeakReference<TrackedObject>(new TrackedObject(obj, id)));
        return id;
    }
    
    public TrackedObject found(Object obj) {
        if (obj == null)
            return null;
        Long id = getUniqueIDFor(obj);
        WeakReference<TrackedObject> found = trackedObjects.get(id);
        if (found != null) {
            return found.get();
        }
        return null;
    }
    
    public Object lookup(Long id) {
        WeakReference<TrackedObject> ref = trackedObjects.get(id);
        return ref.get();
    }

}
