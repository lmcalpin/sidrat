package com.sidrat.event.tracking;

import java.io.Serializable;


public class ExecutionLocation implements Serializable {
    private static final long serialVersionUID = 6558516445801512512L;
    
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
        result = prime * result + ((object == null) ? 0 : object.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExecutionLocation other = (ExecutionLocation) obj;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        if (object == null) {
            if (other.object != null)
                return false;
        } else if (!object.equals(other.object))
            return false;
        return true;
    }
}
