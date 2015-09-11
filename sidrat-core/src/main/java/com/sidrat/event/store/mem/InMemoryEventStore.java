package com.sidrat.event.store.mem;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.SidratFieldChangedEvent;
import com.sidrat.event.SidratLocalVariableEvent;
import com.sidrat.event.SidratMethodEntryEvent;
import com.sidrat.event.SidratMethodExitEvent;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.util.Pair;

/**
 * An implementation of a Sidrat EventStore that retains a history of all program execution events in memory.
 * 
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class InMemoryEventStore implements EventStore {
    public class Partition {
        TreeMap<Long, SidratExecutionEvent> events = new TreeMap<>();
        TreeMap<Long, SidratMethodEntryEvent> entries = new TreeMap<>();
        TreeMap<Long, SidratMethodExitEvent> exits = new TreeMap<>();
        TreeMap<Long, SidratFieldChangedEvent> fields = new TreeMap<>();
        TreeMap<Long, SidratLocalVariableEvent> locals = new TreeMap<>();
        Multimap<Long, Pair<Long, TrackedObject>> fieldHistory = ArrayListMultimap.create();
        Multimap<String, Pair<Long, TrackedObject>> localsHistory = ArrayListMultimap.create();
        Multimap<String, SidratExecutionEvent> executions = ArrayListMultimap.create();
        Multimap<Long, Pair<Long, String>> objectFields = ArrayListMultimap.create();
    }
    private static Map<String, Partition> PARTITION_MAP = new ConcurrentHashMap<>();
    
    TreeMap<Long, SidratExecutionEvent> events;
    TreeMap<Long, SidratMethodEntryEvent> entries;
    TreeMap<Long, SidratMethodExitEvent> exits;
    TreeMap<Long, SidratFieldChangedEvent> fields;
    TreeMap<Long, SidratLocalVariableEvent> locals;
    Multimap<Long, Pair<Long, TrackedObject>> fieldHistory;
    Multimap<String, Pair<Long, TrackedObject>> localsHistory;
    Multimap<String, SidratExecutionEvent> executions;
    Multimap<Long, Pair<Long, String>> objectFields;
    
    public InMemoryEventStore(boolean forWriting, String partitionName) {
        Partition partition;
        if (forWriting) {
            partition = new Partition();
            PARTITION_MAP.put(partitionName, partition);
        } else {
            partition = PARTITION_MAP.get(partitionName);
        }
        events = partition.events;
        entries = partition.entries;
        exits = partition.exits;
        fields = partition.fields;
        locals = partition.locals;
        fieldHistory = partition.fieldHistory;
        localsHistory = partition.localsHistory;
        executions = partition.executions;
        objectFields = partition.objectFields;
    }
    
    @Override
    public void store(SidratExecutionEvent event) {
        executions.put(event.getClassName() + ":" + event.getMethodName() + "#" + event.getLineNumber(), event);
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
        Pair<Long, String> fieldInfo = new Pair<>(event.getTrackedValue().getUniqueID(), event.getVariableName());
        if (!objectFields.get(event.getOwnerUniqueID()).contains(fieldInfo))
            objectFields.put(event.getOwnerUniqueID(), fieldInfo);
        fieldHistory.put(event.getTrackedValue().getUniqueID(), new Pair<>(event.getTime(), event.getTrackedValue()));
        fields.put(event.getTime(), event);
    }

    @Override
    public void store(SidratLocalVariableEvent event) {
        localsHistory.put(event.getUniqueID(), new Pair<>(event.getTime(), event.getTrackedValue()));
        locals.put(event.getTime(), event);
    }

    @Override
    public void close() {
    }
}
