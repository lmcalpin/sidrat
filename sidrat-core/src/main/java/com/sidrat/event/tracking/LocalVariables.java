package com.sidrat.event.tracking;

import java.util.HashMap;
import java.util.Map;

import com.sidrat.instrument.LocalVariable;
import com.sidrat.util.Pair;

import javassist.CtBehavior;

public class LocalVariables {
    private Map<String, TrackedVariable> trackedVariables;
    
    public LocalVariables() {
        this.trackedVariables = new HashMap<String, TrackedVariable>();
    }
    
    public void found(CtBehavior ctBehavior, LocalVariable var) {
        Pair<Integer,Integer> lineNumberRange = var.getLineNumberRange();
        found(ctBehavior.getDeclaringClass().getName(), ctBehavior.getName(), var.getName(), lineNumberRange);
    }
    
    public void found(String className, String method, String var, Pair<Integer,Integer> lineNumberRange) {
        String id = TrackedVariable.getIdentity(className, method, var);
        if (!trackedVariables.containsKey(id))
            trackedVariables.put(id, new TrackedVariable(id, var, lineNumberRange));
    }
    
    public TrackedVariable lookup(String className, String method, String var) {
        String id = TrackedVariable.getIdentity(className, method, var);
        return trackedVariables.get(id);
    }
}
