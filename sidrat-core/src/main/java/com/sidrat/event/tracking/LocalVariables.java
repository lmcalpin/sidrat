package com.sidrat.event.tracking;

import bytecodeparser.analysis.LocalVariable;

import java.util.HashMap;
import java.util.Map;

import javassist.CtBehavior;

import com.sidrat.util.Pair;

public class LocalVariables {
    private Map<String, TrackedVariable> trackedVariables;
    
    public LocalVariables() {
        this.trackedVariables = new HashMap<String, TrackedVariable>();
    }
    
    public void found(CtBehavior ctBehavior, LocalVariable var) {
        Pair<Integer,Integer> lineNumberRange = getLineNumberRange(ctBehavior, var);
        found(ctBehavior.getDeclaringClass().getName(), ctBehavior.getName(), var.name, lineNumberRange);
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
    
    public static Pair<Integer,Integer> getLineNumberRange(CtBehavior ctBehavior, LocalVariable lv) {
        int rangeStart = lv.getValidityRange()[0];
        int rangeEnd = lv.getValidityRange()[1];
        int lineNumberStart = ctBehavior.getMethodInfo().getLineNumber(rangeStart) - 1;
        int lineNumberEnd = lastLineNumber(ctBehavior, rangeStart, rangeEnd, lineNumberStart);
        return new Pair<Integer,Integer>(lineNumberStart, lineNumberEnd);
    }
    
    private static int lastLineNumber(CtBehavior ctBehavior, int rangeStart, int rangeEnd, int lineNumberStart) {
        int lineNumberEnd = ctBehavior.getMethodInfo().getLineNumber(rangeEnd);
        if (rangeStart == rangeEnd) {
            return lineNumberStart;
        }
        // when inside a loop, the last bytecode may correspond to a line number *before* the point where the 
        // variable was declared; in this case, we recursively attempt to determine the last line number *after* the
        // variable was declared where the variable is still in scope
        if (lineNumberEnd < lineNumberStart) {
            return lastLineNumber(ctBehavior, rangeStart, rangeEnd - 1, lineNumberStart);
        }
        return lineNumberEnd;
    }
}
