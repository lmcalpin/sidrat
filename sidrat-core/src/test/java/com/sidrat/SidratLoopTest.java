package com.sidrat;

import com.metatrope.testprogram.ForLoopTest;

import com.sidrat.event.SidratExecutionEvent;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SidratLoopTest {
    @BeforeClass
    public static void setup() {
        SidratRecorder recorder = new SidratRecorder();
        recorder.store("sidrat-loop-test").record(ForLoopTest.class.getName());
    }
    
    // verify we do not have a regression where the beginning of the for loop generates two events
    @Test
    public void testLocalVariableTracking() {
        SidratReplay replay = new SidratReplay("sidrat-loop-test");
        replay.withSource("src/test/java");
        SidratExecutionEvent event = replay.gotoEvent(2);
        SidratExecutionEvent event2 = replay.gotoEvent(3);
        Assert.assertFalse(event.getLineNumber() == event2.getLineNumber());
    }
}
