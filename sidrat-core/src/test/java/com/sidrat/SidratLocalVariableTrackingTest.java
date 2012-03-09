package com.sidrat;

import com.metatrope.testprogram.ForLocalVariableTest;

import java.util.Map;

import com.sidrat.event.tracking.TrackedObject;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SidratLocalVariableTrackingTest {
    @BeforeClass
    public static void setup() {
        SidratDebugger debugger = new SidratDebugger();
        debugger.store("sidrat-localvars-test").debug(ForLocalVariableTest.class.getName());
    }
    
    @Test
    public void testLocalVariableTracking() {
        SidratReplay replay = new SidratReplay("sidrat-localvars-test");
        replay.withSource("src/test/java");
        replay.gotoEvent(1);
        Map<String,TrackedObject> locals = replay.locals();
        Assert.assertEquals(1, locals.size());
        Assert.assertTrue(locals.keySet().contains("firstVariable"));
        Assert.assertEquals("1", locals.get("firstVariable").getValue());
        replay.gotoEvent(2);
        locals = replay.locals();
        Assert.assertEquals(2, locals.size());
        Assert.assertTrue(locals.keySet().contains("secondVariable"));
        Assert.assertEquals("2", locals.get("secondVariable").getValue());
        replay.gotoEvent(3);
        locals = replay.locals();
        Assert.assertEquals(3, locals.size());
        Assert.assertTrue(locals.keySet().contains("thirdVariable"));
        Assert.assertEquals("3", locals.get("thirdVariable").getValue());
        replay.gotoEvent(4);
        locals = replay.locals();
        Assert.assertEquals(3, locals.size());
        Assert.assertEquals("4", locals.get("thirdVariable").getValue());
    }
}
