package com.sidrat.event;

import javax.annotation.Nonnull;

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
    private Pair<Integer, Integer> variableValidityRange; // for local variables

    public SidratLocalVariableEvent(Long time) {
        super(time);
    }

    public static SidratLocalVariableEvent variableChanged(@Nonnull TrackedObject val, @Nonnull TrackedVariable var) {
        SidratLocalVariableEvent event = new SidratLocalVariableEvent(SidratRegistry.instance().getRecorder().getClock().current());
        event.value = val;
        event.variableValidityRange = new Pair<Integer, Integer>(var.getLineNumberStart(), var.getLineNumberEnd());
        event.variableName = var.getName();
        event.uniqueID = var.getId();
        return event;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public String getVariableName() {
        return variableName;
    }

    public TrackedObject getTrackedValue() {
        return value;
    }

    public Pair<Integer, Integer> getVariableValidityRange() {
        return variableValidityRange;
    }

    /**
     * @return a unique identifier for the object that this field points to
     */
    public Long getReferenceUniqueID() {
        if (value == null)
            return null;
        return value.getUniqueID();
    }
}
