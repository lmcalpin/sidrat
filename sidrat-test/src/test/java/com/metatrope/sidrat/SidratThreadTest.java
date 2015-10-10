package com.metatrope.sidrat;

import com.metatrope.sidrat.testprogram.ForThreadTest;

import java.util.Map;

import com.sidrat.BaseRecorderTest;
import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.tracking.CapturedLocalVariableValue;

import org.junit.Assert;
import org.junit.Test;

public class SidratThreadTest extends BaseRecorderTest {
    public SidratThreadTest(EventRepositoryFactory factory) {
        super(factory);
    }

    @Test
    public void testThreadTracking() {
        recorder.record(ForThreadTest.class.getName());
        replay.gotoEvent(1);
        Long lastEvent = 1L;
        SidratExecutionEvent event = replay.gotoEvent(1);
        while (event != null) {
            lastEvent = event.getTime();
            event = replay.readNext();
        }
        Map<String, CapturedLocalVariableValue> locals = replay.locals();
        System.out.println(locals + "@" + lastEvent);
        String iArr = locals.get("i").getCurrentValue().getValueAsString();
        Assert.assertEquals("[95]", iArr);
    }
}
