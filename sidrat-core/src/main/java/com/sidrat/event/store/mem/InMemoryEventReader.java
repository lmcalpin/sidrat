package com.sidrat.event.store.mem;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.SidratLocalVariableEvent;
import com.sidrat.event.SidratMethodEntryEvent;
import com.sidrat.event.store.EventReader;
import com.sidrat.event.tracking.CapturedFieldValue;
import com.sidrat.event.tracking.CapturedLocalVariableValue;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.event.tracking.TrackedVariable;
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
    public Map<String, CapturedLocalVariableValue> locals(Long now) {
        SidratMethodEntryEvent methodEntry = store.entries.floorEntry(now).getValue();
        Long t = store.locals.ceilingKey(methodEntry.getTime());
        Map<String, CapturedLocalVariableValue> locals = new HashMap<>();
        while (t != null && t <= now) {
            SidratLocalVariableEvent var = store.locals.get(t);
            t = store.locals.higherKey(t);
            TrackedVariable variable = new TrackedVariable(var.getUniqueID(), var.getVariableName(), var.getVariableValidityRange());
            CapturedLocalVariableValue value = new CapturedLocalVariableValue(t, variable, var.getTrackedValue());
            locals.put(var.getVariableName(), value);
        }
        return locals;
    }

    @Override
    public Map<String, CapturedFieldValue> eval(Long time, Long objectID) {
        Collection<Pair<Long, String>> objectsFieldIds = store.objectFields.get(objectID);
        Map<String, CapturedFieldValue> fields = new HashMap<>();
        for (Pair<Long, String> fieldInfo : objectsFieldIds) {
            Long fieldId = fieldInfo.getValue1();
            String fieldName = fieldInfo.getValue2();
            List<Pair<Long, TrackedObject>> fieldHistory = fieldHistory(fieldId);
            TrackedObject latestFieldValue = fieldHistory.stream().max((p1, p2) -> p1.getValue1() > p2.getValue1() ? 1 : -1).get().getValue2();
            CapturedFieldValue cfv = new CapturedFieldValue(time, objectID, latestFieldValue);
            fields.put(fieldName, cfv);
        }
        return fields;
    }

    @Override
    public List<SidratExecutionEvent> executions(String className, String methodName, int lineNumber) {
        return (List<SidratExecutionEvent>) store.executions.get(className + ":" + methodName + "#" + lineNumber);
    }

    @Override
    public List<Pair<Long, TrackedObject>> fieldHistory(Long fieldID) {
        return (List<Pair<Long, TrackedObject>>) store.fieldHistory.get(fieldID);
    }

    @Override
    public List<Pair<Long, TrackedObject>> localVariableHistory(String localVariableID) {
        return (List<Pair<Long, TrackedObject>>) store.localsHistory.get(localVariableID);
    }

}
