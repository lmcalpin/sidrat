package com.sidrat.event.store.jpa;

import java.util.Collections;

import com.google.common.collect.ImmutableMap;
import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.SidratFieldChangedEvent;
import com.sidrat.event.SidratLocalVariableEvent;
import com.sidrat.event.SidratMethodEntryEvent;
import com.sidrat.event.SidratMethodExitEvent;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.store.jpa.model.EncounteredClass;
import com.sidrat.event.store.jpa.model.EncounteredField;
import com.sidrat.event.store.jpa.model.EncounteredMethod;
import com.sidrat.event.store.jpa.model.EncounteredObject;
import com.sidrat.event.store.jpa.model.EncounteredThread;
import com.sidrat.event.store.jpa.model.EncounteredVariable;
import com.sidrat.event.store.jpa.model.Execution;
import com.sidrat.event.store.jpa.model.FieldUpdate;
import com.sidrat.event.store.jpa.model.LocalVariableUpdate;
import com.sidrat.event.store.jpa.model.MethodEntry;
import com.sidrat.event.store.jpa.model.MethodExit;
import com.sidrat.event.tracking.TrackedObject;

/**
 * This implementation of EventStore uses HSQLDB to store events and run state information (changes to variables and fields).
 * It is highly unlikely that this will work even remotely well for any "real world" project. It is only intended to support
 * debugging small projects as a proof of concept.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class JPAEventStore implements EventStore {
    JPADAO dao;

    public JPAEventStore(String partition) {
        dao = new JPADAO(partition);
        eraseExisting(partition);
    }

    @Override
    public void close() {
    }

    private void eraseExisting(String partition) {
        dao.deleteAll();
    }

    // TODO: we should be able to do this without synchronization
    private synchronized EncounteredObject foundObject(TrackedObject trackedObject) {
        EncounteredObject encounteredObject = dao.findSingle("FROM EncounteredObject WHERE name = :name", Collections.singletonMap("name", trackedObject.getUniqueID()));
        if (encounteredObject != null)
            return encounteredObject;
        encounteredObject = new EncounteredObject();
        encounteredObject.setName(trackedObject.getUniqueID());
        EncounteredClass encounteredObjectType = new EncounteredClass();
        encounteredObjectType.setName(trackedObject.getClassName());
        encounteredObject.setClazz(dao.findOrCreate(encounteredObjectType));
        dao.persist(encounteredObject);
        return encounteredObject;
    }

    @Override
    public void store(SidratExecutionEvent event) {
        MethodEntry methodEntry = dao.findByTime(MethodEntry.class, event.getMethodEntryTime());
        Execution execution = new Execution();
        execution.setTime(event.getTime());
        execution.setMethodEntry(methodEntry);
        execution.setLineNumber(event.getLineNumber());
        dao.persist(execution);
    }

    @Override
    public void store(SidratFieldChangedEvent event) {
        EncounteredField field = new EncounteredField();
        field.setName(event.getUniqueID());
        field.setObject(foundObject(event.getOwner()));
        field = dao.findOrCreate(field);
        FieldUpdate update = new FieldUpdate();
        update.setTime(event.getTime());
        update.setField(field);
        update.setRef(foundObject(event.getTrackedValue()));
        update.setValue(event.getTrackedValue().getValueAsString());
        dao.persist(update);
    }

    @Override
    public void store(SidratLocalVariableEvent event) {
        EncounteredMethod method = dao.findSingle("FROM EncounteredMethod WHERE clazz.name = :className AND name = :methodName",
                ImmutableMap.<String, Object> builder().put("className", event.getClassName()).put("methodName", event.getMethodName()).build());
        if (method == null) {
            throw new IllegalStateException("Instrumenting a local variable event in a method we haven't encountered yet?!");
        }
        EncounteredVariable localVariable = new EncounteredVariable();
        localVariable.setName(event.getUniqueID());
        localVariable.setVariableName(event.getVariableName());
        localVariable.setMethod(method);
        localVariable.setClazz(event.getClassName());
        localVariable.setRangeStart(event.getScopeStart());
        localVariable.setRangeEnd(event.getScopeEnd());
        localVariable = dao.findOrCreate(localVariable);
        LocalVariableUpdate update = new LocalVariableUpdate();
        update.setTime(event.getTime());
        update.setLocalVariable(localVariable);
        update.setObject(foundObject(event.getTrackedValue()));
        update.setValue(event.getTrackedValue().getValueAsString());
        dao.persist(update);
    }

    @Override
    public void store(SidratMethodEntryEvent event) {
        MethodEntry methodEntry = new MethodEntry();
        methodEntry.setTime(event.getTime());
        if (event.getExecutionContext().getObject() != null) {
            methodEntry.setObject(foundObject(event.getExecutionContext().getObject()));
        }
        EncounteredMethod encounteredMethod = new EncounteredMethod();
        EncounteredClass encounteredClass = new EncounteredClass();
        encounteredClass.setName(event.getExecutionContext().getClassName());
        encounteredMethod.setClazz(dao.findOrCreate(encounteredClass));
        encounteredMethod.setName(event.getExecutionContext().getMethodName());
        methodEntry.setMethod(dao.findOrCreate(encounteredMethod));
        EncounteredThread encounteredThread = new EncounteredThread();
        encounteredThread.setName(event.getExecutionContext().getThreadName());
        methodEntry.setThread(dao.findOrCreate(encounteredThread));
        dao.persist(methodEntry);
    }

    @Override
    public void store(SidratMethodExitEvent event) {
        MethodExit methodExit = new MethodExit();
        methodExit.setTime(event.getTime());
        MethodEntry methodEntry = dao.findByTime(MethodEntry.class, event.getMethodEntryTime());
        methodExit.setMethodEntry(methodEntry);
        if (event.getReturns() != null) {
            EncounteredObject encounteredObject = new EncounteredObject();
            encounteredObject.setName(event.getReturns().getUniqueID());
            EncounteredClass encounteredObjectType = new EncounteredClass();
            encounteredObjectType.setName(event.getReturns().getClassName());
            encounteredObject.setClazz(dao.findOrCreate(encounteredObjectType));
            methodExit.setObject(encounteredObject);
            methodExit.setValue(event.getReturns().getValueAsString());
        }
        dao.persist(methodExit);
    }
}
