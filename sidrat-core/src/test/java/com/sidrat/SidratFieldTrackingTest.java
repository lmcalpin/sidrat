package com.sidrat;

import com.metatrope.testprogram.ForFieldTrackingTest;

import java.util.Map;

import com.sidrat.event.tracking.CapturedFieldValue;
import com.sidrat.event.tracking.CapturedLocalVariableValue;
import com.sidrat.event.tracking.TrackedObject;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SidratFieldTrackingTest {
    @BeforeClass
    public static void setup() {
        SidratRecorder recorder = SidratRegistry.instance().newRecorder();
        recorder.store("sidrat-fields-test").record(ForFieldTrackingTest.class.getName());
    }
    
    @Test
    public void testFieldTracking() {
        SidratReplay replay = new SidratReplay("sidrat-fields-test");
        replay.withSource("src/test/java");
        replay.gotoEvent(3);
        Map<String,CapturedLocalVariableValue> locals = replay.locals();
        Assert.assertEquals(1, locals.size());
        Assert.assertTrue(locals.keySet().contains("theClass"));

        TrackedObject trackedObject = locals.get("theClass").getCurrentValue();
        Map<String,CapturedFieldValue> fieldValues = replay.eval(trackedObject);
        System.out.println(fieldValues);
        
        replay.gotoEvent(6);
        fieldValues = replay.eval(trackedObject);
        Assert.assertEquals(2, fieldValues.size());
    }

}
