package com.sidrat.event;

import com.sidrat.util.Objects;

public class SidratFieldChangedEvent extends SidratEvent {
    private Class<?> ownerClass; // non-null if the event involves a change to a field
    private Long objectInstanceID;
    private String uniqueID;
    private String variableName;
    private Object value;
    
    public SidratFieldChangedEvent(Long time) {
        super(time);
    }

    public static SidratFieldChangedEvent fieldChanged(Object obj, Object val, String name) {
        SidratFieldChangedEvent event = new SidratFieldChangedEvent(SidratClock.instance().current());
        event.ownerClass = obj.getClass();
        event.objectInstanceID = Objects.getUniqueIdentifier(obj);
        event.value = val;
        event.variableName = name;
        event.uniqueID = event.ownerClass.getName() + "." + name;
        return event;
    }

    public Class<?> getOwnerClass() {
        return ownerClass;
    }

    public Long getObjectInstanceID() {
        return objectInstanceID;
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
}
