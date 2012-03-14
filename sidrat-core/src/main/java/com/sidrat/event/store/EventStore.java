package com.sidrat.event.store;

import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.SidratFieldChangedEvent;
import com.sidrat.event.SidratLocalVariableEvent;
import com.sidrat.event.SidratMethodEntryEvent;
import com.sidrat.event.SidratMethodExitEvent;

public interface EventStore {
    public void store(SidratExecutionEvent event);
    public void store(SidratMethodEntryEvent event);
    public void store(SidratMethodExitEvent event);
    public void store(SidratFieldChangedEvent event);
    public void store(SidratLocalVariableEvent event);
    public void close();
}
