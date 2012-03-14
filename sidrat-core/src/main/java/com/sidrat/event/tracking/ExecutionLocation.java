package com.sidrat.event.tracking;


public class ExecutionLocation {
    private final TrackedObject object;
    private final String className, methodName;
    
    public ExecutionLocation(TrackedObject object, String className, String methodName) {
        this.object = object;
        this.className = className;
        this.methodName = methodName;
    }
    
    public TrackedObject getObject() {
        return object;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }
}
