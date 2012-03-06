package com.sidrat.event;

import com.sidrat.SidratDebugger;
import com.sidrat.event.tracking.TrackedObject;


public class SidratFieldChangedEvent extends SidratEvent {
    private Object owner;
    private String uniqueID;
    private String variableName;
    private Object value;
    private Long referenceUniqueID;
    
    public SidratFieldChangedEvent(Long time) {
        super(time);
    }

    public static SidratFieldChangedEvent fieldChanged(Object obj, Object val, String name) {
        SidratFieldChangedEvent event = new SidratFieldChangedEvent(SidratDebugger.instance().getClock().current());
        event.owner = obj;
        event.value = val;
        event.variableName = name;
        event.uniqueID = event.getClass().getName() + "." + name;
        return event;
    }
    
    public static SidratFieldChangedEvent fieldChanged(Object obj, TrackedObject val, String name) {
        SidratFieldChangedEvent event = fieldChanged(obj,  val.getValue(), name);
        event.referenceUniqueID = val.getUniqueID();
        return event;
    }

    public Object getOwner() {
        return owner;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public String getVariableName() {
        return variableName;
    }

    public Object getValue() {
        return value;
    }

    /**
     * @return a unique identifier for the object that this field points to
     */
    public Long getReferenceUniqueID() {
        return referenceUniqueID;
    }
}
