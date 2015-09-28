package com.metatrope.sidrat;

import com.metatrope.sidrat.testprogram.ForLocalVariableTest;

import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.util.Tuple3;

import org.junit.Assert;
import org.junit.Test;

public class SidratReplayTest extends BaseRecorderTest {
    public SidratReplayTest(EventRepositoryFactory factory) {
        super(factory);
    }

    @Test
    public void testLookupSourceCode() {
        recorder.record(ForLocalVariableTest.class.getName());
        replay.withSource("src/test/java");
        SidratExecutionEvent event = replay.gotoEvent(1);
        String sourceCode = replay.lookupSourceCode(event);
        Assert.assertEquals("        int firstVariable = 1;", sourceCode);
        event = replay.gotoEvent(2);
        sourceCode = replay.lookupSourceCode(event);
        Assert.assertEquals("        int secondVariable = 2;", sourceCode);
        event = replay.gotoEvent(3);
        sourceCode = replay.lookupSourceCode(event);
        Assert.assertEquals("        int thirdVariable = 3;", sourceCode);
    }

    @Test
    public void testBreakpointSplit() {
        Tuple3<String, String, Integer> split = replay.split("com.metatrope.testprogram.ClassName.method:42");
        Assert.assertEquals("com.metatrope.testprogram.ClassName", split.getValue1());
        Assert.assertEquals("method", split.getValue2());
        Assert.assertEquals(new Integer(42), split.getValue3());
    }
}
