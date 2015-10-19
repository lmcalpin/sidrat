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
    public void testLocalArrayTracking() {
        recorder.record(() -> {
            int[] i = new int[] { 0 };
            char[] c = new char[] { 'a', 'b' };
            byte[] b = new byte[] { 0x00 };
            long[] l = new long[] { 15L };
            short[] s = new short[] { 13, 14, 15 };
            float[] f = new float[] { 13.0f };
            double[] d = new double[] { 13.0d };
            boolean[] bool = new boolean[] { true, false };
            i[0] = 3;
            c[0] = 'z';
            b[0] = 0x01;
            l[0] = 235L;
            s[0] = 133;
            f[0] *= 2;
            d[0] *= 10.1;
            bool[0] = false;
        });
        replay.gotoEvent(1);
        Assert.assertEquals(1, replay.locals().keySet().size());
        Assert.assertTrue(replay.locals().keySet().contains("i"));
        Assert.assertEquals("[0]", replay.locals().get("i").getCurrentValue().getValueAsString());
        replay.gotoEvent(2);
        Assert.assertEquals(2, replay.locals().keySet().size());
        Assert.assertTrue(replay.locals().keySet().contains("c"));
        replay.gotoEvent(7);
        Assert.assertEquals(7, replay.locals().keySet().size());
        Assert.assertTrue(replay.locals().keySet().contains("b"));
        replay.gotoEvent(16);
        Assert.assertEquals(8, replay.locals().keySet().size());
        Assert.assertEquals("[3]", replay.locals().get("i").getCurrentValue().getValueAsString());
        replay.gotoEnd();
        Assert.assertEquals("[26.0]", replay.locals().get("f").getCurrentValue().getValueAsString());
        Assert.assertEquals("[false, false]", replay.locals().get("bool").getCurrentValue().getValueAsString());
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
