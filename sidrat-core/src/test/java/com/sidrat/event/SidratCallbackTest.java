package com.sidrat.event;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SidratCallbackTest {
    @Before
    public void init() {
        while (SidratCallback.currentFrame() != null) {
            SidratCallback.popFrame();
        }
    }
    
    @Test
    public void testEnterPushesCurrentFrameToStack() {
        SidratCallback.enter("com.test", "foo");
        Assert.assertEquals("com.test", SidratCallback.currentFrame().getClassName());
        Assert.assertEquals("foo", SidratCallback.currentFrame().getMethodName());
    }
    
    @Test
    public void testExitPopsCurrentFrameToStack() {
        Assert.assertNull(SidratCallback.currentFrame());
        SidratCallback.enter("com.test", "foo");
        Assert.assertNotNull(SidratCallback.currentFrame());
        SidratCallback.exit();
        Assert.assertNull(SidratCallback.currentFrame());
    }
}
