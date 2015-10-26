package com.sidrat.event.store.mem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.SidratFieldChangedEvent;
import com.sidrat.event.SidratLocalVariableEvent;
import com.sidrat.event.SidratMethodEntryEvent;
import com.sidrat.event.SidratMethodExitEvent;
import com.sidrat.event.store.EventRepository;
import com.sidrat.event.tracking.CapturedFieldValue;
import com.sidrat.event.tracking.CapturedLocalVariableValue;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.event.tracking.TrackedVariable;
import com.sidrat.util.Pair;

/**
 * An implementation of a Sidrat EventStore that retains a history of all program execution events in memory.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class InMemoryEventRepository implements EventRepository {
    TreeMap<Long, SidratExecutionEvent> events = new TreeMap<>();
    TreeMap<Long, SidratMethodEntryEvent> entries = new TreeMap<>();
    TreeMap<Long, SidratMethodExitEvent> exits = new TreeMap<>();
    TreeMap<Long, SidratFieldChangedEvent> fields = new TreeMap<>();
    TreeMap<Long, List<SidratLocalVariableEvent>> locals = new TreeMap<>();
    Multimap<String, Pair<Long, TrackedObject>> fieldHistory = ArrayListMultimap.create();
    Multimap<String, Pair<Long, TrackedObject>> localsHistory = ArrayListMultimap.create();
    Multimap<String, SidratExecutionEvent> executions = ArrayListMultimap.create();
    Multimap<String, Pair<String, String>> objectFields = ArrayListMultimap.create();

    public InMemoryEventRepository() {
    }

    @Override
    public void close() {
    }

    @Override
    public Map<String, CapturedFieldValue> eval(Long time, String objectID) {
        Collection<Pair<String, String>> objectsFieldIds = objectFields.get(objectID);
        Map<String, CapturedFieldValue> fields = new HashMap<>();
        for (Pair<String, String> fieldInfo : objectsFieldIds) {
            String fieldId = fieldInfo.getValue1();
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
        return (List<SidratExecutionEvent>) executions.get(className + ":" + methodName + "#" + lineNumber);
    }

    @Override
    public List<Pair<Long, TrackedObject>> fieldHistory(String fieldID) {
        return (List<Pair<Long, TrackedObject>>) fieldHistory.get(fieldID);
    }

    @Override
    public SidratExecutionEvent find(Long time) {
        return events.higherEntry(time - 1).getValue();
    }

    @Override
    public SidratExecutionEvent findFirst() {
        return events.firstEntry().getValue();
    }

    @Override
    public SidratExecutionEvent findNext(SidratExecutionEvent lastEvent) {
        try {
            Entry<Long, SidratExecutionEvent> nextEntry = events.higherEntry(lastEvent.getTime());
            if (nextEntry == null)
                return null;
            return nextEntry.getValue();
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public SidratExecutionEvent findPrev(SidratExecutionEvent lastEvent) {
        return events.lowerEntry(lastEvent.getTime()).getValue();
    }

    @Override
    public Map<String, CapturedLocalVariableValue> locals(Long now) {
        SidratExecutionEvent currentEvent = find(now);
        Map<String, CapturedLocalVariableValue> ret = new HashMap<>();
        int currentLineNumber = find(now).getLineNumber();
        for (List<SidratLocalVariableEvent> vars : locals.headMap(now, true).values()) {
            for (SidratLocalVariableEvent var : vars) {
                if (var.getScopeStart() <= currentLineNumber && var.getScopeEnd() >= currentLineNumber && var.getClassName().equals(currentEvent.getClassName()) && var.getMethodName().equals(currentEvent.getMethodName())) {
                    TrackedVariable variable = new TrackedVariable(var.getClassName(), var.getMethodName(), var.getVariableName(), var.getVariableValidityRange());
                    CapturedLocalVariableValue value = new CapturedLocalVariableValue(var.getTime(), variable, var.getTrackedValue());
                    ret.put(var.getVariableName(), value);
                }
            }
        }
        return ret;
    }

    @Override
    public List<Pair<Long, TrackedObject>> localVariableHistory(String localVariableID) {
        return (List<Pair<Long, TrackedObject>>) localsHistory.get(localVariableID);
    }

    @Override
    public void store(SidratExecutionEvent event) {
        executions.put(event.getClassName() + ":" + event.getMethodName() + "#" + event.getLineNumber(), event);
        events.put(event.getTime(), event);
    }

    @Override
    public void store(SidratFieldChangedEvent event) {
        Pair<String, String> fieldInfo = new Pair<>(event.getTrackedValue().getUniqueID(), event.getVariableName());
        if (!objectFields.get(event.getOwnerUniqueID()).contains(fieldInfo))
            objectFields.put(event.getOwnerUniqueID(), fieldInfo);
        fieldHistory.put(event.getTrackedValue().getUniqueID(), new Pair<>(event.getTime(), event.getTrackedValue()));
        fields.put(event.getTime(), event);
    }

    @Override
    public void store(SidratLocalVariableEvent event) {
        localsHistory.put(event.getUniqueID(), new Pair<>(event.getTime(), event.getTrackedValue()));
        List<SidratLocalVariableEvent> events = locals.get(event.getTime());
        if (events == null) {
            events = new ArrayList<>();
        }
        events.add(event);
        locals.put(event.getTime(), events);
    }

    @Override
    public void store(SidratMethodEntryEvent event) {
        entries.put(event.getTime(), event);
    }

    @Override
    public void store(SidratMethodExitEvent event) {
        exits.put(event.getTime(), event);
    }
}
