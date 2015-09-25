package com.sidrat;

import java.util.Map;

import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.tracking.CapturedLocalVariableValue;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("TODO")
public class SidratIncTest extends BaseRecorderTest {
    public SidratIncTest(EventRepositoryFactory factory) {
        super(factory);
    }

    // verify we do not have a regression where the beginning of the for loop generates two events
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
        replay.gotoEvent(1);
        SidratExecutionEvent event = replay.event;
        int i = 0;
        while (event != null) {
            i++;
            System.out.println(i + ": " + event.getLineNumber());
            event = replay.readNext();
        }
        Map<String, CapturedLocalVariableValue> locals = replay.locals();
        System.out.println(locals);
    }
}
