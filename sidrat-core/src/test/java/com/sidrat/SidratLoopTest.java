package com.sidrat;

import com.metatrope.testprogram.ForLoopTest;

import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.store.EventRepositoryFactory;

import org.junit.Assert;
import org.junit.Test;

public class SidratLoopTest extends BaseRecorderTest {
    public SidratLoopTest(EventRepositoryFactory factory) {
        super(factory);
    }

    // verify we do not have a regression where the beginning of the for loop generates two events
    @Test
    public void testLocalVariableTracking() {
        recorder.record(ForLoopTest.class.getName());
        SidratExecutionEvent event = replay.gotoEvent(2);
        SidratExecutionEvent event2 = replay.gotoEvent(3);
        Assert.assertFalse(event.getLineNumber() == event2.getLineNumber());
    }
}
