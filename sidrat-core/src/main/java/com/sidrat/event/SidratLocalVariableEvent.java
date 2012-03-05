package com.sidrat.event;

import com.sidrat.event.tracking.TrackedVariable;
import com.sidrat.util.Pair;

public class SidratLocalVariableEvent extends SidratEvent {
    private String uniqueID;
    private String variableName;
    private Object value;
    private Pair<Integer,Integer> variableValidityRange; // for local variables
    
    public SidratLocalVariableEvent(Long time) {
        super(time);
    }

    public static SidratLocalVariableEvent variableChanged(Object val, TrackedVariable var) {
        SidratLocalVariableEvent event = new SidratLocalVariableEvent(SidratClock.instance().current());
        event.value = val;
        event.variableValidityRange = new Pair<Integer,Integer>(var.getLineNumberStart(), var.getLineNumberEnd());
        event.variableName = var.getName();
        event.uniqueID = var.getId();
        return event;
    }
    
    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Pair<Integer, Integer> getVariableValidityRange() {
        return variableValidityRange;
    }

    public void setVariableValidityRange(Pair<Integer, Integer> variableValidityRange) {
        this.variableValidityRange = variableValidityRange;
    }
    
    public void setVariableValidityRange(Integer rangeStart, Integer rangeEnd) {
        this.setVariableValidityRange(new Pair<Integer,Integer>(rangeStart, rangeEnd));
    }

}

