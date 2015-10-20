package com.sidrat.junit;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Strings;
import com.sidrat.SidratRecorder;
import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.store.EventReader;
import com.sidrat.event.tracking.CapturedLocalVariableValue;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.util.Pair;

import org.junit.Assert;
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
    private List<String> historyVars;

    public SidratRunListener(SidratRecorder recorder, EventReader eventReader, List<String> historyVars) {
        this.recorder = recorder;
        this.eventReader = eventReader;
        this.historyVars = historyVars;
    }

    private void render(Failure failure) {
        System.out.println(failure.getTestHeader() + " FAILED!!!!!!!");

        long lastEventCounter = recorder.getClock().current();
        if (lastEventCounter == 0) {
            Assert.fail("A Sidrat instrumented test was not executed");
        }
        SidratExecutionEvent exec = eventReader.find(lastEventCounter);
        System.out.println("Finished in: " + exec.getClassName() + "#" + exec.getMethodName() + "@" + exec.getLineNumber());
        Collection<CapturedLocalVariableValue> locals = eventReader.locals(lastEventCounter).values();

        StringBuilder sb = new StringBuilder();
        for (CapturedLocalVariableValue local : locals) {
            sb.append(Strings.padEnd(local.getVariable().getName(), 35, ' '));
            sb.append(" | ");
            sb.append(Strings.padEnd(local.getCurrentValue().getClassName(), 35, ' '));
            sb.append(" | ");
            sb.append(local.getCurrentValue().getValueAsString());
            sb.append("\n");
            if (historyVars.contains(local.getVariable().getName())) {
                List<Pair<Long, TrackedObject>> history = eventReader.localVariableHistory(local.getVariable().getId());
                for (Pair<Long, TrackedObject> historicalValue : history) {
                    sb.append("   - " + historicalValue.getValue1() + ": " + historicalValue.getValue2().getValueAsString());
                    sb.append("\n");
                }
            }
        }
        System.out.println(sb.toString());
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        render(failure);
        super.testFailure(failure);
    }

}
