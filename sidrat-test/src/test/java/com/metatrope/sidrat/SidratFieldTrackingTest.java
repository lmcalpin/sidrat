package com.metatrope.sidrat;

import com.metatrope.sidrat.testprogram.ForFieldTrackingTest;
import com.metatrope.sidrat.testprogram.ForFieldTrackingTest.ClassWithFields;

import java.util.Map;

import com.google.gson.Gson;
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
        replay.gotoEvent(3);
        Map<String, CapturedLocalVariableValue> locals = replay.locals();
        Assert.assertEquals(1, locals.size());
        Assert.assertTrue(locals.keySet().contains("theClass"));

        TrackedObject trackedObject = locals.get("theClass").getCurrentValue();
        replay.gotoEvent(6);
        Map<String, CapturedFieldValue> fieldValues = replay.eval(trackedObject);
        Assert.assertEquals(2, fieldValues.size());
    }

    @Test
    public void testLocalValueUpdatedOnMethodExit() {
        recorder.record(ForFieldTrackingTest.class.getName());
        replay.gotoEnd();
        Map<String, CapturedLocalVariableValue> locals = replay.locals();
        Assert.assertEquals(1, locals.size());
        Assert.assertTrue(locals.keySet().contains("theClass"));

        TrackedObject trackedObject = locals.get("theClass").getCurrentValue();
        String jsonEncodedValue = trackedObject.getValueAsString();
        Gson gson = new Gson();
        ClassWithFields trackedValue = gson.fromJson(jsonEncodedValue, ClassWithFields.class);
        Assert.assertEquals(7, trackedValue.a);
    }
}
