package com.sidrat.event.tracking;

import bytecodeparser.analysis.LocalVariable;
import javassist.CtBehavior;

import com.sidrat.util.Pair;

public class TrackedVariable {
    private String id;
    private ExecutionLocation frame;
    private String name;
    private int lineNumberStart;
    private int lineNumberEnd;

    public TrackedVariable(String id, String variableName, Pair<Integer,Integer> lineNumberRange) {
        this.id = id;
        this.name = variableName;
        this.lineNumberStart = lineNumberRange.getValue1();
        this.lineNumberEnd = lineNumberRange.getValue2();
    }

    public String getName() {
        return name;
    }

    public int getLineNumberStart() {
        return lineNumberStart;
    }

    public int getLineNumberEnd() {
        return lineNumberEnd;
    }

    public String getId() {
        return id;
    }

    public ExecutionLocation getFrame() {
        return frame;
    }

    public static String getIdentity(String className, String methodName, String variableName) {
        StringBuilder sb = new StringBuilder();
        sb.append(className);
        sb.append('.');
        sb.append(methodName);
        sb.append(':');
        sb.append(variableName);
        return sb.toString();
    }
}
