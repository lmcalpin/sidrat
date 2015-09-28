package com.metatrope;

import com.metatrope.sidrat.testprogram.ForFieldTrackingTest;

import java.util.Map;

import com.sidrat.BaseRecorderTest;
import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.tracking.CapturedFieldValue;
import com.sidrat.event.tracking.CapturedLocalVariableValue;
import com.sidrat.event.tracking.TrackedObject;

import org.junit.Assert;
import org.junit.Test;

public class SidratFieldTrackingTest extends BaseRecorderTest {
    public SidratFieldTrackingTest(EventRepositoryFactory factory) {
        super(factory);
    }

    @Test
    public void testFieldTracking() {
        recorder.record(ForFieldTrackingTest.class.getName());
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
