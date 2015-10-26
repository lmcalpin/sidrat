package com.sidrat.event.store.jpa;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.SidratMethodEntryEvent;
import com.sidrat.event.store.EventReader;
import com.sidrat.event.store.jpa.model.EncounteredField;
import com.sidrat.event.store.jpa.model.EncounteredVariable;
import com.sidrat.event.store.jpa.model.Execution;
import com.sidrat.event.store.jpa.model.FieldUpdate;
import com.sidrat.event.store.jpa.model.LocalVariableUpdate;
import com.sidrat.event.store.jpa.model.MethodEntry;
import com.sidrat.event.tracking.CapturedFieldValue;
import com.sidrat.event.tracking.CapturedLocalVariableValue;
import com.sidrat.event.tracking.ExecutionLocation;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.util.Pair;

/**
 * Reads a Sidrat recording from a database.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class JPAEventReader implements EventReader {
    JPADAO dao;

    public JPAEventReader(String name) {
        dao = new JPADAO(name);
    }

    private SidratExecutionEvent convert(Execution execution) {
        MethodEntry methodEntry = execution.getMethodEntry();
        TrackedObject trackedObject = methodEntry.getObject() != null ? new TrackedObject(methodEntry.getObject().getClazz().getName(), null, methodEntry.getObject().getName()) : null;
        ExecutionLocation executionLocation = new ExecutionLocation(trackedObject, methodEntry.getThread().getName(), methodEntry.getMethod().getClazz().getName(), methodEntry.getMethod().getName());
        SidratMethodEntryEvent methodEntryEvent = new SidratMethodEntryEvent(methodEntry.getTime(), executionLocation, methodEntry.getThread().getId(), methodEntry.getThread().getName());
        SidratExecutionEvent event = new SidratExecutionEvent(execution.getTime(), methodEntryEvent, execution.getLineNumber());
        return event;
    }

    @Override
    public Map<String, CapturedFieldValue> eval(Long time, String objectID) {
        List<EncounteredField> fields = dao.find("FROM EncounteredField WHERE owner.name = :objectid", Collections.singletonMap("objectid", String.valueOf(objectID)));
        Map<String, CapturedFieldValue> values = Maps.newHashMap();
        for (EncounteredField field : fields) {
            String fieldName = field.getName();
            FieldUpdate update = dao.findFirst("FROM FieldUpdate WHERE field = :field AND time <= :time ORDER BY time DESC", ImmutableMap.<String, Object> builder().put("field", field).put("time", time).build());
            if (update != null) {
                values.put(fieldName, update.asCapturedFieldValue());
            }
        }
        return values;
    }

    @Override
    public List<SidratExecutionEvent> executions(String className, String methodName, int lineNumber) {
        Map<String, Object> params = new HashMap<>();
        params.put("clazz", className);
        params.put("method", methodName);
        params.put("lineNumber", lineNumber);
        List<Execution> executions = dao.find("FROM Execution WHERE methodEntry.method.clazz.name=:clazz AND methodEntry.method.name=:method AND lineNumber=:lineNumber", params);
        return executions.stream().map(e -> convert(e)).collect(Collectors.toList());
    }

    @Override
    public List<Pair<Long, TrackedObject>> fieldHistory(String fieldID) {
        List<FieldUpdate> updates = dao.find("FROM FieldUpdate WHERE field.name = :fieldid ORDER BY time DESC", ImmutableMap.<String, Object> builder().put("fieldid", String.valueOf(fieldID)).build());
        return updates.stream().map(lvu -> new Pair<Long, TrackedObject>(lvu.getTime(), lvu.asTrackedObject())).collect(Collectors.toList());
    }

    @Override
    public SidratExecutionEvent find(Long time) {
        Execution execution = dao.findByTime(Execution.class, time);
        if (execution == null)
            return null;
        SidratExecutionEvent event = convert(execution);
        return event;
    }

    @Override
    public SidratExecutionEvent findFirst() {
        Execution execution = dao.findFirst("FROM Execution ORDER BY time ASC");
        if (execution == null)
            return null;
        SidratExecutionEvent event = convert(execution);
        return event;
    }

    @Override
    public SidratExecutionEvent findNext(SidratExecutionEvent lastEvent) {
        Execution execution = dao.findFirst("FROM Execution WHERE time > :time ORDER BY time ASC", Collections.singletonMap("time", lastEvent.getTime()));
        if (execution == null)
            return null;
        SidratExecutionEvent event = convert(execution);
        return event;
    }

    @Override
    public SidratExecutionEvent findPrev(SidratExecutionEvent lastEvent) {
        Execution execution = dao.findFirst("FROM Execution WHERE time < :time ORDER BY time ASC", Collections.singletonMap("time", lastEvent.getTime()));
        if (execution == null)
            return null;
        SidratExecutionEvent event = convert(execution);
        return event;
    }

    @Override
    public Map<String, CapturedLocalVariableValue> locals(Long time) {
        Execution execution = dao.findByTime(Execution.class, time);
        MethodEntry method = execution.getMethodEntry();
        List<EncounteredVariable> localVariables = dao.find("FROM EncounteredVariable WHERE method = :method", Collections.singletonMap("method", method.getMethod()));
        Map<String, CapturedLocalVariableValue> locals = new HashMap<>();
        for (EncounteredVariable localVariable : localVariables) {
            if (localVariable.getRangeStart() <= execution.getLineNumber() && localVariable.getRangeEnd() >= execution.getLineNumber()) {
                LocalVariableUpdate latestUpdate = dao.findFirst("FROM LocalVariableUpdate WHERE localVariable = :var AND time <= :time ORDER BY time DESC", ImmutableMap.<String, Object> builder().put("var", localVariable).put("time", time).build());
                if (latestUpdate == null) {
                    locals.put(localVariable.getVariableName(), null);
                } else {
                    locals.put(localVariable.getVariableName(), latestUpdate.asCapturedLocalVariableValue());
                }
            }
        }
        return locals;
    }

    @Override
    public List<Pair<Long, TrackedObject>> localVariableHistory(String localVariableID) {
        List<LocalVariableUpdate> updates = dao.find("FROM LocalVariableUpdate WHERE localVariable.name = :var ORDER BY time DESC", ImmutableMap.<String, Object> builder().put("var", localVariableID).build());
        return updates.stream().map(lvu -> new Pair<Long, TrackedObject>(lvu.getTime(), lvu.asTrackedObject())).collect(Collectors.toList());
    }
}
