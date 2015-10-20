package com.metatrope.sidrat;

import com.metatrope.sidrat.testprogram.ForLocalVariableTest;

import java.util.List;
import java.util.Map;

import com.sidrat.BaseRecorderTest;
import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.tracking.CapturedLocalVariableValue;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.util.Pair;

import org.junit.Assert;
import org.junit.Test;

public class SidratLocalVariableTrackingTest extends BaseRecorderTest {
    public SidratLocalVariableTrackingTest(EventRepositoryFactory factory) {
        super(factory);
    }

    @Test
    public void testLocalVariableTracking() {
        recorder.record(ForLocalVariableTest.class.getName());
        replay.gotoEvent(1);
        Map<String, CapturedLocalVariableValue> locals = replay.locals();
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

    @Test
    public void testVariableTracking() {
        recorder.record(() -> {
            int sum = 0;
            for (int i = 0; i < 5; i++) {
                sum += 5;
            }
            System.out.println(sum);
        });
        replay.gotoEnd();
        Assert.assertEquals(2, replay.locals().keySet().size());
        Assert.assertTrue(replay.locals().keySet().contains("sum"));
        Assert.assertEquals("25", replay.locals().get("sum").getCurrentValue().getValueAsString());
        List<Pair<Long, TrackedObject>> historicalValues = replay.history(replay.locals().get("sum"));
        Assert.assertEquals(6, historicalValues.size());
        for (Pair<Long, TrackedObject> value : historicalValues) {
            System.out.println(value.getValue1() + ":" + value.getValue2().getValueAsString());
        }
    }
}
