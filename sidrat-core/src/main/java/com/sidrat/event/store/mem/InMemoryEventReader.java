package com.sidrat.event.store.mem;

import java.util.List;
import java.util.Map;

import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.store.EventReader;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.tracking.CapturedFieldValue;
import com.sidrat.event.tracking.CapturedLocalVariableValue;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.util.Pair;

/**
 * Implementation of a Sidrat EventReader that allows us to browse the events stored in the InMemoryEventStore.  
 * 
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class InMemoryEventReader implements EventReader {
    private InMemoryEventStore store;
    
    public InMemoryEventReader(InMemoryEventStore store) {
        this.store = store;
    }

    @Override
    public SidratExecutionEvent find(Long time) {
        return store.events.higherEntry(time - 1).getValue();
    }

    @Override
    public SidratExecutionEvent findFirst() {
        return store.events.firstEntry().getValue();
    }

    @Override
    public SidratExecutionEvent findNext(SidratExecutionEvent lastEvent) {
        return store.events.higherEntry(lastEvent.getTime()).getValue();
    }

    @Override
    public SidratExecutionEvent findPrev(SidratExecutionEvent lastEvent) {
        return store.events.lowerEntry(lastEvent.getTime()).getValue();
    }

    @Override
    public Map<String, CapturedLocalVariableValue> locals(Long time) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, CapturedFieldValue> eval(Long time, Long objectID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SidratExecutionEvent> executions(String className, String methodName, int lineNumber) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Pair<Long, TrackedObject>> fieldHistory(Long fieldID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Pair<Long, TrackedObject>> localVariableHistory(String localVariableID) {
        // TODO Auto-generated method stub
        return null;
    }

}
