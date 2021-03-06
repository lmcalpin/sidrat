package com.metatrope.sidrat;

import java.util.Map;

import com.sidrat.BaseRecorderTest;
import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.tracking.CapturedLocalVariableValue;

import org.junit.Assert;
import org.junit.Test;

public class SidratIncTest extends BaseRecorderTest {
    public SidratIncTest(EventRepositoryFactory factory) {
        super(factory);
    }

    @Test
    public void testLocalVariableTracking() {
        recorder.record(() -> {
            int s = 0;
            for (int i = 0; i < 10; i++) {
                s++;
                s += 1;
                s -= 2;
            }
            System.out.println(s);
        });
        Long time = 0L;
        SidratExecutionEvent event = replay.gotoEvent(1);
        int i = 0;
        while (event != null) {
            i++;
            System.out.println(i + ": " + event.getLineNumber());
            time = event.getTime();
            event = replay.readNext();
        }

        Map<String, CapturedLocalVariableValue> locals = replay.locals();
        Assert.assertTrue(locals.keySet().contains("s"));
        Assert.assertTrue(locals.keySet().contains("i"));
        Assert.assertEquals(new Long(43L), time);
    }
}
