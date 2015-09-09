package com.sidrat;

import com.metatrope.testprogram.ForLocalVariableTest;

import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.util.Tuple3;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SidratReplayTest {
    @BeforeClass
    public static void setup() {
        SidratRecorder recorder = new SidratRecorder();
        recorder.store("sidrat-replay-test").record(ForLocalVariableTest.class.getName());
    }

    @Test
    public void testLookupSourceCode() {
        SidratReplay replay = new SidratReplay("sidrat-replay-test");
        replay.withSource("src/test/java");
        SidratExecutionEvent event = replay.gotoEvent(2);
        String sourceCode = replay.lookupSourceCode(event);
        Assert.assertEquals("        int secondVariable = 2;", sourceCode);
    }

    @Test
    public void testBreakpointSplit() {
        SidratReplay replay = new SidratReplay("sidrat-localvars-test");
        Tuple3<String, String, Integer> split = replay.split("com.metatrope.testprogram.ClassName.method:42");
        Assert.assertEquals("com.metatrope.testprogram.ClassName", split.getValue1());
        Assert.assertEquals("method", split.getValue2());
        Assert.assertEquals(new Integer(42), split.getValue3());
    }
}
