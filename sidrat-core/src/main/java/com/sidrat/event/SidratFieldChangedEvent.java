package com.sidrat.event;


public class SidratFieldChangedEvent extends SidratEvent {
    private Object owner;
    private String uniqueID;
    private String variableName;
    private Object value;
    
    public SidratFieldChangedEvent(Long time) {
        super(time);
    }

    public static SidratFieldChangedEvent fieldChanged(Object obj, Object val, String name) {
        SidratFieldChangedEvent event = new SidratFieldChangedEvent(SidratClock.instance().current());
        event.owner = obj;
        event.value = val;
        event.variableName = name;
        event.uniqueID = event.getClass().getName() + "." + name;
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
}
