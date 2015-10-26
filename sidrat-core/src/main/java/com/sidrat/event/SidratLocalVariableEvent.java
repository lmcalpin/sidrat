package com.sidrat.event;

import com.sidrat.SidratRegistry;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.event.tracking.TrackedVariable;
import com.sidrat.util.Pair;

/**
 * Triggered when a local variable's value changes.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratLocalVariableEvent extends SidratEvent {
    private String uniqueID;
    private String variableName;
    private TrackedObject value;
    private TrackedVariable var;
    private Pair<Integer, Integer> variableValidityRange; // for local variables

    private SidratLocalVariableEvent(Long time) {
        super(time);
    }

    public static SidratLocalVariableEvent variableChanged(TrackedObject val, TrackedVariable var) {
        SidratLocalVariableEvent event = new SidratLocalVariableEvent(SidratRegistry.instance().getRecorder().getClock().current());
        event.value = val;
        event.var = var;
        event.variableValidityRange = new Pair<Integer, Integer>(var.getLineNumberStart(), var.getLineNumberEnd());
        event.variableName = var.getName();
        event.uniqueID = var.getId();
        return event;
    }

    public String getClassName() {
        return var.getClassName();
    }

    public String getMethodName() {
        return var.getMethodName();
    }

    /**
     * @return a unique identifier for the object that this field points to
     */
    public String getReferenceUniqueID() {
        if (value == null)
            return null;
        return value.getUniqueID();
    }

    public Integer getScopeEnd() {
        return variableValidityRange.getValue2();
    }

    public Integer getScopeStart() {
        return variableValidityRange.getValue1();
    }

    public TrackedObject getTrackedValue() {
        return value;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public String getVariableName() {
        return variableName;
    }

    public Pair<Integer, Integer> getVariableValidityRange() {
        return variableValidityRange;
    }

    @Override
    public String toString() {
        return variableName + "(" + uniqueID + ")@" + super.getTime();
    }
}
