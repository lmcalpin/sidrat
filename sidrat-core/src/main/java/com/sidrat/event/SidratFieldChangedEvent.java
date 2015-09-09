package com.sidrat.event;

import com.sidrat.SidratRecorder;
import com.sidrat.event.tracking.TrackedObject;

/**
 * Triggered when a field value changes.
 *  
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratFieldChangedEvent extends SidratEvent {
    private TrackedObject owner;
    private String uniqueID;
    private String variableName;
    private TrackedObject value;
    
    public SidratFieldChangedEvent(Long time) {
        super(time);
    }

    public static SidratFieldChangedEvent fieldChanged(TrackedObject obj, TrackedObject val, String name) {
        SidratFieldChangedEvent event = new SidratFieldChangedEvent(SidratRecorder.instance().getClock().current());
        event.owner = obj;
        event.value = val;
        event.variableName = name;
        event.uniqueID = event.getClass().getName() + "." + name;
        return event;
    }

    public TrackedObject getOwner() {
        return owner;
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

    /**
     * @return a unique identifier for the object that this field points to
     */
    public Long getReferenceUniqueID() {
        if (owner != null) {
            return owner.getUniqueID();
        }
        return null;
    }
}
