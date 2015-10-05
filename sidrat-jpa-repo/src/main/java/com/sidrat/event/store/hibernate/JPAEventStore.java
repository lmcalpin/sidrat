package com.sidrat.event.store.hibernate;

import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.SidratFieldChangedEvent;
import com.sidrat.event.SidratLocalVariableEvent;
import com.sidrat.event.SidratMethodEntryEvent;
import com.sidrat.event.SidratMethodExitEvent;
import com.sidrat.event.store.EventStore;

/**
 * This implementation of EventStore uses HSQLDB to store events and run state information (changes to variables and fields).
 * It is highly unlikely that this will work even remotely well for any "real world" project. It is only intended to support
 * debugging small projects as a proof of concept.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class JPAEventStore implements EventStore {

    public JPAEventStore(String name) {
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public void store(SidratExecutionEvent event) {
        // TODO Auto-generated method stub

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
