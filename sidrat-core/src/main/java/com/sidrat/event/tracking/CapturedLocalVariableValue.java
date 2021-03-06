package com.sidrat.event.tracking;

public class CapturedLocalVariableValue implements ValueTracker {
    private Long time;
    private TrackedVariable variable;
    private TrackedObject currentValue;

    public CapturedLocalVariableValue(Long time, TrackedVariable variable, TrackedObject value) {
        this.time = time;
        this.variable = variable;
        this.currentValue = value;
    }

    @Override
    public TrackedObject getCurrentValue() {
        return currentValue;
    }

    public Long getTime() {
        return time;
    }

    public TrackedVariable getVariable() {
        return variable;
    }

    @Override
    public String toString() {
        return variable.getId() + "@" + time;
    }
}
