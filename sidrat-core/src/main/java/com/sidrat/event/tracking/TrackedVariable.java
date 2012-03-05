package com.sidrat.event.tracking;

import javassist.CtBehavior;
import bytecodeparser.analysis.LocalVariable;

public class TrackedVariable {
    private String id;
    private StackFrame frame;
    private String name;
    private int slot;
    private int lineNumberStart;
    private int lineNumberEnd;

    public TrackedVariable(CtBehavior ctBehavior, LocalVariable lv) {
        this.id = getIdentity(ctBehavior, lv);
        this.frame = frame;
        this.name = lv.name;
        this.slot = lv.getSlot();
        int rangeStart = lv.getValidityRange()[0];
        int rangeEnd = lv.getValidityRange()[1];
        this.lineNumberStart = ctBehavior.getMethodInfo().getLineNumber(rangeStart) - 1;
        this.lineNumberEnd = ctBehavior.getMethodInfo().getLineNumber(rangeEnd);
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

    public StackFrame getFrame() {
        return frame;
    }

    public static String getIdentity(CtBehavior ctBehavior, LocalVariable var) {
        String className = ctBehavior.getDeclaringClass().getName();
        String methodName = ctBehavior.getName();
        return getIdentity(className, methodName, var);
    }
    
    public static String getIdentity(String className, String methodName, LocalVariable var) {
        StringBuilder sb = new StringBuilder();
        sb.append(className);
        sb.append('.');
        sb.append(methodName);
        sb.append(':');
        sb.append(var.name);
        sb.append('#');
        sb.append(var.getSlot());
        return sb.toString();
    }
}
