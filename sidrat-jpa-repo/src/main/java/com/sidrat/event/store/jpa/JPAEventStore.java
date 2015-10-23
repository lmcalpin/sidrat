package com.sidrat.event.store.jpa;

import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.SidratFieldChangedEvent;
import com.sidrat.event.SidratLocalVariableEvent;
import com.sidrat.event.SidratMethodEntryEvent;
import com.sidrat.event.SidratMethodExitEvent;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.store.jpa.model.EncounteredClass;
import com.sidrat.event.store.jpa.model.EncounteredMethod;
import com.sidrat.event.store.jpa.model.EncounteredObject;
import com.sidrat.event.store.jpa.model.EncounteredThread;
import com.sidrat.event.store.jpa.model.Execution;
import com.sidrat.event.store.jpa.model.MethodEntry;

/**
 * This implementation of EventStore uses HSQLDB to store events and run state information (changes to variables and fields).
 * It is highly unlikely that this will work even remotely well for any "real world" project. It is only intended to support
 * debugging small projects as a proof of concept.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class JPAEventStore implements EventStore {
    JPADAO dao;

    public JPAEventStore(String name) {
        dao = new JPADAO(name);
    }

    @Override
    public void close() {
    }

    @Override
    public void store(SidratExecutionEvent event) {
        MethodEntry methodEntry = new MethodEntry();
        methodEntry.setId(event.getMethodEntryTime());
        if (event.getExecutionContext().getObject() != null) {
            EncounteredObject encounteredObject = new EncounteredObject();
            encounteredObject.setId(event.getExecutionContext().getObject().getUniqueID());
            EncounteredClass encounteredObjectType = new EncounteredClass();
            encounteredObjectType.setName(event.getExecutionContext().getObject().getClassName());
            encounteredObject.setClazz(dao.findOrCreate(encounteredObjectType));
            methodEntry.setObject(encounteredObject);
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
        Execution execution = new Execution();
        execution.setId(event.getTime());
        execution.setMethodEntry(methodEntry);
        execution.setLineNumber(event.getLineNumber());
        dao.store(execution);
    }

    @Override
    public void store(SidratFieldChangedEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void store(SidratLocalVariableEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void store(SidratMethodEntryEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void store(SidratMethodExitEvent event) {
        // TODO Auto-generated method stub

    }
}
