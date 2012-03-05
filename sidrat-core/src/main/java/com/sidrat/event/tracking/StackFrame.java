package com.sidrat.event.tracking;

import com.sidrat.util.Objects;

public class StackFrame {
    private final Object object;
    private final String className, methodName;

    public StackFrame(Object object, String className, String methodName) {
        this.object = object;
        this.className = className;
        this.methodName = methodName;
    }

    public StackFrame(String className, String methodName) {
        this(null, className, methodName);
    }

    public Object getObject() {
        return object;
    }
    
    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }
}
