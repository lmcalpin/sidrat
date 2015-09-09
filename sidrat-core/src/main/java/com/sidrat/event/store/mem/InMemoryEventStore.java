package com.sidrat.event.store.mem;

import java.util.TreeMap;

import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.SidratFieldChangedEvent;
import com.sidrat.event.SidratLocalVariableEvent;
import com.sidrat.event.SidratMethodEntryEvent;
import com.sidrat.event.SidratMethodExitEvent;
import com.sidrat.event.store.EventStore;

/**
 * An implementation of a Sidrat EventStore that retains a history of all program execution events in memory.
 * 
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class InMemoryEventStore implements EventStore {
    TreeMap<Long, SidratExecutionEvent> events = new TreeMap<>();
    TreeMap<Long, SidratMethodEntryEvent> entries = new TreeMap<>();
    TreeMap<Long, SidratMethodExitEvent> exits = new TreeMap<>();
    TreeMap<Long, SidratFieldChangedEvent> fields = new TreeMap<>();
    TreeMap<Long, SidratLocalVariableEvent> locals = new TreeMap<>();
    
    public InMemoryEventStore() {
        
    }
    
    @Override
    public void store(SidratExecutionEvent event) {
        events.put(event.getTime(), event);
    }

    @Override
    public void store(SidratMethodEntryEvent event) {
        entries.put(event.getTime(), event);
    }

    @Override
    public void store(SidratMethodExitEvent event) {
        exits.put(event.getTime(), event);
    }

    @Override
    public void store(SidratFieldChangedEvent event) {
        fields.put(event.getTime(), event);
    }

    @Override
    public void store(SidratLocalVariableEvent event) {
        locals.put(event.getTime(), event);
    }

    @Override
    public void close() {
    }
}
