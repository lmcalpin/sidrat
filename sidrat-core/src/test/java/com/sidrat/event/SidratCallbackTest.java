package com.sidrat.event;

import static org.easymock.EasyMock.capture;
import bytecodeparser.analysis.LocalVariable;

import com.sidrat.SidratDebugger;
import com.sidrat.event.store.EventStore;
import com.sidrat.util.Pair;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SidratCallbackTest {
    EventStore mockedEventStore;

    @Before
    public void init() {
        mockedEventStore = EasyMock.createMock(EventStore.class);
        SidratDebugger.instance().store(mockedEventStore);
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

    @Test
    public void testExec() {
        // expectations
        Capture<SidratExecutionEvent> captured = new Capture<SidratExecutionEvent>();
        mockedEventStore.store(capture(captured));
        
        EasyMock.replay(mockedEventStore);
        
        SidratCallback.enter("com.Test", "foo");
        SidratCallback.exec(10);
        
        EasyMock.verify(mockedEventStore);
        
        Assert.assertEquals("com.Test", captured.getValue().getClassName());
        Assert.assertEquals("foo", captured.getValue().getMethodName());
        Assert.assertEquals(10, captured.getValue().getLineNumber());
    }
    
    @Test
    public void testVariableTracking() {
        // expectations
        Capture<SidratLocalVariableEvent> captured = new Capture<SidratLocalVariableEvent>();
        mockedEventStore.store(capture(captured));
        EasyMock.replay(mockedEventStore);
        
        // local variable should have been logged in the LocalVariablesTracker when instrumenting
        SidratDebugger.instance().getLocalVariablesTracker().found("com.Test", "foo", "bar", new Pair<Integer,Integer>(10,11));        
        
        SidratCallback.enter("com.Test", "foo");
        SidratCallback.variableChanged(10, "bar");
        
        EasyMock.verify(mockedEventStore);
        
        Assert.assertEquals("bar", captured.getValue().getVariableName());
        Assert.assertEquals(10, captured.getValue().getValue());
    }
    
    @Test
    public void testFieldTracking() {
        // expectations
        Capture<SidratFieldChangedEvent> captured = new Capture<SidratFieldChangedEvent>();
        mockedEventStore.store(capture(captured));
        
        EasyMock.replay(mockedEventStore);
        
        SidratCallback.enter("com.Test", "foo");
        Object obj = new Object();
        SidratCallback.fieldChanged(obj, 10, "foo");
        
        EasyMock.verify(mockedEventStore);
        
        Assert.assertEquals(obj, captured.getValue().getOwner());
        Assert.assertEquals("foo", captured.getValue().getVariableName());
        Assert.assertEquals(10, captured.getValue().getValue());
    }
    
}
