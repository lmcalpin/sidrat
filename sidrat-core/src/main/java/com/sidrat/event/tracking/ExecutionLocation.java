package com.sidrat.event.tracking;

import java.io.Serializable;
import java.util.Objects;

public class ExecutionLocation implements Serializable {
    private static final long serialVersionUID = 6558516445801512512L;

    private final TrackedObject object;
    private final String threadName, className, methodName;

    public ExecutionLocation(TrackedObject object, String threadName, String className, String methodName) {
        this.object = object;
        this.threadName = threadName;
        this.className = className;
        this.methodName = methodName;
    }

    public TrackedObject getObject() {
        return object;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(threadName, className, methodName);
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

    @Override
    public String toString() {
        return getThreadName() + ":" + getClassName() + "." + getMethodName();
    }
}
