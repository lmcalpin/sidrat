package com.sidrat.event.tracking;

import com.sidrat.util.Pair;

public class TrackedVariable {
    private String id;
    private String name;
    private int lineNumberStart;
    private int lineNumberEnd;

    public TrackedVariable(String id, String variableName, Pair<Integer, Integer> lineNumberRange) {
        this.id = id;
        this.name = variableName;
        this.lineNumberStart = lineNumberRange.getValue1();
        this.lineNumberEnd = lineNumberRange.getValue2();
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

    public String getId() {
        return id;
    }

    public int getLineNumberEnd() {
        return lineNumberEnd;
    }

    public int getLineNumberStart() {
        return lineNumberStart;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getId() + "(" + getLineNumberStart() + ":" + getLineNumberEnd() + ")";
    }
}
