package com.sidrat.event.tracking;

import bytecodeparser.analysis.LocalVariable;

import java.util.HashMap;
import java.util.Map;

import javassist.CtBehavior;


public class LocalVariables {
    private Map<String, TrackedVariable> observedVariables;
    
    public LocalVariables() {
        this.observedVariables = new HashMap<String, TrackedVariable>();
    }
    
    public String found(CtBehavior ctBehavior, LocalVariable var) {
        String id = TrackedVariable.getIdentity(ctBehavior, var);
        if (!observedVariables.containsKey(id))
            observedVariables.put(id, new TrackedVariable(ctBehavior, var));
        return id;
    }
    
    public TrackedVariable lookup(String id) {
        return observedVariables.get(id);
    }
}
