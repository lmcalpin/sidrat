package com.sidrat.event.store.hibernate;

import java.util.List;
import java.util.Map;

import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.store.EventReader;
import com.sidrat.event.tracking.CapturedFieldValue;
import com.sidrat.event.tracking.CapturedLocalVariableValue;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.util.Pair;

/**
 * Reads a Sidrat recording from a database.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class JPAEventReader implements EventReader {

    public JPAEventReader(String name) {

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
    public SidratExecutionEvent find(Long time) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SidratExecutionEvent findFirst() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SidratExecutionEvent findNext(SidratExecutionEvent lastEvent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SidratExecutionEvent findPrev(SidratExecutionEvent lastEvent) {
        // TODO Auto-generated method stub
        return null;
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
