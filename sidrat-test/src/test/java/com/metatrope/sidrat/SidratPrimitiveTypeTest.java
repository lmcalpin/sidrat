package com.metatrope.sidrat;

import com.sidrat.BaseRecorderTest;
import com.sidrat.event.store.EventRepositoryFactory;

import org.junit.Assert;
import org.junit.Test;

public class SidratPrimitiveTypeTest extends BaseRecorderTest {
    public SidratPrimitiveTypeTest(EventRepositoryFactory factory) {
        super(factory);
    }

    @Test
    public void testLocalVariableTracking() {
        recorder.record(() -> {
            int i = 0;
            char c = 'a';
            byte b = 0x00;
            long l = 15L;
            short s = 13;
            float f = 13.0f;
            double d = 13.0d;
            boolean bool = true;
            i = 3;
            c = 'z';
            b = 0x01;
            l = 235L;
            s = 133;
            f *= 2;
            d *= 10.1;
            bool = false;
        });
        replay.gotoEvent(1);
        Assert.assertEquals(1, replay.locals().keySet().size());
        Assert.assertTrue(replay.locals().keySet().contains("i"));
        Assert.assertEquals("0", replay.locals().get("i").getCurrentValue().getValueAsString());
        replay.gotoEvent(2);
        Assert.assertEquals(2, replay.locals().keySet().size());
        Assert.assertTrue(replay.locals().keySet().contains("c"));
        replay.gotoEvent(7);
        Assert.assertEquals(7, replay.locals().keySet().size());
        Assert.assertTrue(replay.locals().keySet().contains("b"));
        replay.gotoEvent(16);
        Assert.assertEquals(8, replay.locals().keySet().size());
        Assert.assertEquals("3", replay.locals().get("i").getCurrentValue().getValueAsString());
    }
}
