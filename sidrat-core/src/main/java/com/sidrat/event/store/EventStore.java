package com.sidrat.event.store;

import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.SidratFieldChangedEvent;
import com.sidrat.event.SidratLocalVariableEvent;

public interface EventStore {
    public void store(SidratExecutionEvent event);
    public void store(SidratFieldChangedEvent event);
    public void store(SidratLocalVariableEvent event);
    public void close();
}
