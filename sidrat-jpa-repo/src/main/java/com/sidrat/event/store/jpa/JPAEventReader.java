package com.sidrat.event.store.jpa;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.SidratMethodEntryEvent;
import com.sidrat.event.store.EventReader;
import com.sidrat.event.store.jpa.model.Execution;
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
        TrackedObject trackedObject = methodEntry.getObject() != null ? new TrackedObject(methodEntry.getObject().getClazz().getName(), null, methodEntry.getObject().getId()) : null;
        ExecutionLocation executionLocation = new ExecutionLocation(trackedObject, methodEntry.getThread().getName(), methodEntry.getMethod().getClazz().getName(), methodEntry.getMethod().getName());
        SidratMethodEntryEvent methodEntryEvent = new SidratMethodEntryEvent(methodEntry.getId(), executionLocation, methodEntry.getThread().getId(), methodEntry.getThread().getName());
        SidratExecutionEvent event = new SidratExecutionEvent(execution.getId(), methodEntryEvent, execution.getLineNumber());
        return event;
    }

    @Override
    public Map<String, CapturedFieldValue> eval(Long time, Long objectID) {
        // TODO Auto-generated method stub
        return null;
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
    public List<Pair<Long, TrackedObject>> fieldHistory(Long fieldID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SidratExecutionEvent find(Long time) {
        Execution execution = dao.findSingle("FROM Execution WHERE id=:time", Collections.singletonMap("time", time));
        if (execution == null)
            return null;
        SidratExecutionEvent event = convert(execution);
        return event;
    }

    @Override
    public SidratExecutionEvent findFirst() {
        Execution execution = dao.findFirst("FROM Execution ORDER BY id ASC");
        if (execution == null)
            return null;
        SidratExecutionEvent event = convert(execution);
        return event;
    }

    @Override
    public SidratExecutionEvent findNext(SidratExecutionEvent lastEvent) {
        Execution execution = dao.findFirst("FROM Execution WHERE id > :time ORDER BY id ASC", Collections.singletonMap("time", lastEvent.getTime()));
        if (execution == null)
            return null;
        SidratExecutionEvent event = convert(execution);
        return event;
    }

    @Override
    public SidratExecutionEvent findPrev(SidratExecutionEvent lastEvent) {
        Execution execution = dao.findFirst("FROM Execution WHERE id < :time ORDER BY id ASC", Collections.singletonMap("time", lastEvent.getTime()));
        if (execution == null)
            return null;
        SidratExecutionEvent event = convert(execution);
        return event;
    }

    @Override
    public Map<String, CapturedLocalVariableValue> locals(Long time) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Pair<Long, TrackedObject>> localVariableHistory(String localVariableID) {
        // TODO Auto-generated method stub
        return null;
    }
}
