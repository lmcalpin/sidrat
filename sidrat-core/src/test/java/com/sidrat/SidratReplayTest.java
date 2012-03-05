package com.sidrat;

import com.metatrope.testprogram.ForFieldTrackingTest;
import com.metatrope.testprogram.ForLocalVariableTest;

import java.util.Map;

import com.sidrat.event.SidratEvent;
import com.sidrat.event.SidratExecutionEvent;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SidratReplayTest {
    @BeforeClass
    public static void setup() {
        SidratDebugger debugger = new SidratDebugger();
        debugger.store("sidrat-localvars-test").debug(ForLocalVariableTest.class.getName());
        debugger.store("sidrat-fields-test").debug(ForFieldTrackingTest.class.getName());
    }
    
    @Test
    public void testLookupSourceCode() {
        SidratReplay replay = new SidratReplay("sidrat-localvars-test");
        replay.withSource("src/test/java");
        SidratExecutionEvent event = replay.gotoEvent(2);
        String sourceCode = replay.lookupSourceCode(event);
        Assert.assertEquals("        int secondVariable = 2;", sourceCode);
    }
    
    @Test
    public void testLocalVariableTracking() {
        SidratReplay replay = new SidratReplay("sidrat-localvars-test");
        replay.withSource("src/test/java");
        replay.gotoEvent(1);
        Map<String,Object> locals = replay.locals();
        Assert.assertEquals(1, locals.size());
        Assert.assertTrue(locals.keySet().contains("firstVariable"));
        Assert.assertEquals("1", locals.get("firstVariable"));
        replay.gotoEvent(2);
        locals = replay.locals();
        Assert.assertEquals(2, locals.size());
        Assert.assertTrue(locals.keySet().contains("secondVariable"));
        Assert.assertEquals("2", locals.get("secondVariable"));
        replay.gotoEvent(3);
        locals = replay.locals();
        Assert.assertEquals(3, locals.size());
        Assert.assertTrue(locals.keySet().contains("thirdVariable"));
        Assert.assertEquals("3", locals.get("thirdVariable"));
        replay.gotoEvent(4);
        locals = replay.locals();
        Assert.assertEquals(3, locals.size());
        Assert.assertEquals("4", locals.get("thirdVariable"));
    }

    @Test
    public void testFieldTracking() {
        SidratReplay replay = new SidratReplay("sidrat-fields-test");
        replay.withSource("src/test/java");
        replay.gotoEvent(3);
        Map<String,Object> locals = replay.locals();
        Assert.assertEquals(1, locals.size());
        Assert.assertTrue(locals.keySet().contains("theClass"));
        
        // TODO: lookup fields in the 'theClass' object
        // TODO: map should hold pointer to object instance in our db, not the original toString
    }

}
