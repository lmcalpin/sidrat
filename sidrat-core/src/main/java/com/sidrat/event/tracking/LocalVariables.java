package com.sidrat.event.tracking;

import bytecodeparser.analysis.LocalVariable;

import java.util.HashMap;
import java.util.Map;

import javassist.CtBehavior;

import com.sidrat.util.Pair;


public class LocalVariables {
    private Map<String, TrackedVariable> observedVariables;
    
    public LocalVariables() {
        this.observedVariables = new HashMap<String, TrackedVariable>();
    }
    
    public void found(CtBehavior ctBehavior, LocalVariable var) {
        Pair<Integer,Integer> lineNumberRange = getLineNumberRange(ctBehavior, var);
        found(ctBehavior.getDeclaringClass().getName(), ctBehavior.getName(), var.name, lineNumberRange);
    }
    
    public void found(String className, String method, String var, Pair<Integer,Integer> lineNumberRange) {
        String id = TrackedVariable.getIdentity(className, method, var);
        if (!observedVariables.containsKey(id))
            observedVariables.put(id, new TrackedVariable(id, var, lineNumberRange));
    }
    
    public TrackedVariable lookup(String className, String method, String var) {
        String id = TrackedVariable.getIdentity(className, method, var);
        return observedVariables.get(id);
    }
    
    public static Pair<Integer,Integer> getLineNumberRange(CtBehavior ctBehavior, LocalVariable lv) {
        int rangeStart = lv.getValidityRange()[0];
        int rangeEnd = lv.getValidityRange()[1];
        int lineNumberStart = ctBehavior.getMethodInfo().getLineNumber(rangeStart) - 1;
        int lineNumberEnd = ctBehavior.getMethodInfo().getLineNumber(rangeEnd);
        return new Pair<Integer,Integer>(lineNumberStart, lineNumberEnd);
    }
}
