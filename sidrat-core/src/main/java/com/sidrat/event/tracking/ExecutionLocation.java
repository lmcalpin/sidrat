package com.sidrat.event.tracking;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ExecutionLocation implements Serializable {
    private static final long serialVersionUID = 6558516445801512512L;

    private final TrackedObject object;
    private final String threadName, className, methodName;
    private Map<TrackedVariable, TrackedObject> encounteredVariables = new HashMap<>();

    public ExecutionLocation(TrackedObject object, String threadName, String className, String methodName) {
        this.object = object;
        this.threadName = threadName;
        this.className = className;
        this.methodName = methodName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExecutionLocation other = (ExecutionLocation) obj;
        return Objects.equals(threadName, other.threadName) && Objects.equals(className, other.className) && Objects.equals(methodName, other.methodName);
    }

    public String getClassName() {
        return className;
    }

    public Map<TrackedVariable, TrackedObject> getEncounteredVariables() {
        return encounteredVariables;
    }

    public String getMethodName() {
        return methodName;
    }

    public TrackedObject getObject() {
        return object;
    }

    public String getThreadName() {
        return threadName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName);
    }

    @Override
    public String toString() {
        return getClassName() + "." + getMethodName();
    }

    public void track(TrackedVariable var, TrackedObject val) {
        assert var != null;
        assert val != null;
        encounteredVariables.put(var, val);
    }
}
