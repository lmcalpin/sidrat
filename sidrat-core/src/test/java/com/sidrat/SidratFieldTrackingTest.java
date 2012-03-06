package com.sidrat;

import com.metatrope.testprogram.ForFieldTrackingTest;
import com.metatrope.testprogram.ForLocalVariableTest;

import java.util.Map;

import com.sidrat.event.SidratExecutionEvent;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SidratFieldTrackingTest {
    @BeforeClass
    public static void setup() {
        SidratDebugger debugger = new SidratDebugger();
        debugger.store("sidrat-fields-test").debug(ForFieldTrackingTest.class.getName());
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
