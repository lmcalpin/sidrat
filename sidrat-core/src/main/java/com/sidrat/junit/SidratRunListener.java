package com.sidrat.junit;

import java.util.Collection;

import com.google.common.base.Strings;
import com.sidrat.SidratRecorder;
import com.sidrat.event.store.EventReader;
import com.sidrat.event.store.hsqldb.HsqldbEventReader;
import com.sidrat.event.store.hsqldb.HsqldbEventStore;
import com.sidrat.event.store.mem.InMemoryEventReader;
import com.sidrat.event.tracking.CapturedLocalVariableValue;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Dump local variables if a test fails.
 * 
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratRunListener extends RunListener {
    private final SidratRecorder recorder;
    private final EventReader eventReader;
    
    public SidratRunListener(SidratRecorder recorder, EventReader eventReader) {
        this.recorder = recorder;
        this.eventReader = eventReader;
    }
    
    @Override
    public void testFailure(Failure failure) throws Exception {
        render(failure);
        super.testFailure(failure);
    }

    private void render(Failure failure) {
        System.out.println(failure.getTestHeader() + " FAILED!!!!!!!");

        long lastEventCounter = recorder.getClock().current();
        Collection<CapturedLocalVariableValue> locals = eventReader.locals(lastEventCounter).values();
        
        StringBuilder sb = new StringBuilder();
        for (CapturedLocalVariableValue local : locals) {
            sb.append(Strings.padEnd(local.getVariable().getName(), 35, ' '));
            sb.append(" | ");
            sb.append(Strings.padEnd(local.getCurrentValue().getClassName(), 35, ' '));
            sb.append(" | ");
            sb.append(local.getCurrentValue().getValueAsString());
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }

}
