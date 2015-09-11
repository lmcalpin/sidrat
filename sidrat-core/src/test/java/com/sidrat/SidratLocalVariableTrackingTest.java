package com.sidrat;

import com.metatrope.testprogram.ForLocalVariableTest;

import java.util.Map;

import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.tracking.CapturedLocalVariableValue;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SidratLocalVariableTrackingTest extends BaseRecorderTest {
    public SidratLocalVariableTrackingTest(EventRepositoryFactory factory) {
        super(factory);
    }

    @Test
    public void testLocalVariableTracking() {
        recorder.record(ForLocalVariableTest.class.getName());
        replay.withSource("src/test/java");
        replay.gotoEvent(1);
        Map<String,CapturedLocalVariableValue> locals = replay.locals();
        Assert.assertEquals(1, locals.size());
        Assert.assertTrue(locals.keySet().contains("firstVariable"));
        Assert.assertEquals("1", locals.get("firstVariable").getCurrentValue().getValueAsString());
        replay.gotoEvent(2);
        locals = replay.locals();
        Assert.assertEquals(2, locals.size());
        Assert.assertTrue(locals.keySet().contains("secondVariable"));
        Assert.assertEquals("2", locals.get("secondVariable").getCurrentValue().getValueAsString());
        replay.gotoEvent(3);
        locals = replay.locals();
        Assert.assertEquals(3, locals.size());
        Assert.assertTrue(locals.keySet().contains("thirdVariable"));
        Assert.assertEquals("3", locals.get("thirdVariable").getCurrentValue().getValueAsString());
        replay.gotoEvent(4);
        locals = replay.locals();
        Assert.assertEquals(3, locals.size());
        Assert.assertEquals("4", locals.get("thirdVariable").getCurrentValue().getValueAsString());
    }
}
