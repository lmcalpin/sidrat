package com.sidrat.event.tracking;

import java.util.HashMap;
import java.util.Map;

import com.sidrat.util.Pair;

public class LocalVariables {
    private Map<String, TrackedVariable> trackedVariables;

    public LocalVariables() {
        this.trackedVariables = new HashMap<String, TrackedVariable>();
    }

    public void found(String className, String method, String var, int start, int end) {
        String id = TrackedVariable.getIdentity(className, method, var);
        if (!trackedVariables.containsKey(id))
            trackedVariables.put(id, new TrackedVariable(id, var, new Pair<Integer, Integer>(start, end)));
    }

    public TrackedVariable lookup(String className, String method, String var) {
        String id = TrackedVariable.getIdentity(className, method, var);
        return trackedVariables.get(id);
    }
}
