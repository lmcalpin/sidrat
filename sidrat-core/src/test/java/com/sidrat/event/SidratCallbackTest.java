package com.sidrat.event;

import static org.easymock.EasyMock.capture;

import com.sidrat.SidratRegistry;
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
        SidratRegistry.instance().getRecorder().store(mockedEventStore);
        SidratCallback.startRecording();
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
        SidratCallback.exit(null);
        Assert.assertNull(SidratCallback.currentFrame());
    }

    @Test
    public void testMethodEntry() {
        // expectations
        Capture<SidratMethodEntryEvent> capturedMethodEntry = new Capture<SidratMethodEntryEvent>();
        mockedEventStore.store(capture(capturedMethodEntry));
        
        EasyMock.replay(mockedEventStore);
        
        SidratCallback.enter("com.Test", "foo");
        
        EasyMock.verify(mockedEventStore);
        
        Assert.assertEquals("com.Test", capturedMethodEntry.getValue().getClassName());
        Assert.assertEquals("foo", capturedMethodEntry.getValue().getMethodName());
    }
    
    @Test
    public void testExec() {
        // expectations
        Capture<SidratMethodEntryEvent> capturedMethodEntry = new Capture<SidratMethodEntryEvent>();
        Capture<SidratExecutionEvent> capturedExec = new Capture<SidratExecutionEvent>();
        mockedEventStore.store(capture(capturedMethodEntry));
        mockedEventStore.store(capture(capturedExec));
        
        EasyMock.replay(mockedEventStore);
        
        SidratCallback.enter("com.Test", "foo");
        SidratCallback.exec(10);
        
        EasyMock.verify(mockedEventStore);
        
        Assert.assertEquals("com.Test", capturedExec.getValue().getClassName());
        Assert.assertEquals("foo", capturedExec.getValue().getMethodName());
        Assert.assertEquals(10, capturedExec.getValue().getLineNumber());
    }
    
    @Test
    public void testVariableTracking() {
        // expectations
        Capture<SidratMethodEntryEvent> capturedMethodEntry = new Capture<SidratMethodEntryEvent>();
        Capture<SidratLocalVariableEvent> capturedLocalVariable = new Capture<SidratLocalVariableEvent>();
        mockedEventStore.store(capture(capturedMethodEntry));
        mockedEventStore.store(capture(capturedLocalVariable));
        EasyMock.replay(mockedEventStore);
        
        // local variable should have been logged in the LocalVariablesTracker when instrumenting
        SidratCallback.enter("com.Test", "foo");
        SidratCallback.variableChanged("com.Test", "foo", 10, "bar", 10, 100);
        
        EasyMock.verify(mockedEventStore);
        
        Assert.assertEquals("bar", capturedLocalVariable.getValue().getVariableName());
        Assert.assertEquals("10", capturedLocalVariable.getValue().getTrackedValue().getValueAsString());
    }
    
    @Test
    public void testFieldTracking() {
        // expectations
        Capture<SidratMethodEntryEvent> capturedMethodEntry = new Capture<SidratMethodEntryEvent>();
        Capture<SidratFieldChangedEvent> capturedField = new Capture<SidratFieldChangedEvent>();
        mockedEventStore.store(capture(capturedMethodEntry));
        mockedEventStore.store(capture(capturedField));
        
        EasyMock.replay(mockedEventStore);
        
        SidratCallback.enter("com.Test", "foo");
        Object obj = new Object();
        SidratCallback.fieldChanged(obj, 10, "foo");
        
        EasyMock.verify(mockedEventStore);
        
        Assert.assertEquals(obj.getClass().getName(), capturedField.getValue().getOwner().getClassName());
        Assert.assertEquals("foo", capturedField.getValue().getVariableName());
        Assert.assertEquals("10", capturedField.getValue().getTrackedValue().getValueAsString());
    }
    
    @Test
    public void testIgnoreDuplicateExecs() {
        // expectations
        Capture<SidratMethodEntryEvent> capturedMethodEntry = new Capture<SidratMethodEntryEvent>();
        Capture<SidratExecutionEvent> capturedExec = new Capture<SidratExecutionEvent>();
        mockedEventStore.store(capture(capturedMethodEntry));
        mockedEventStore.store(capture(capturedExec));
        
        EasyMock.replay(mockedEventStore);
        
        SidratCallback.enter("com.Test", "foo");
        SidratCallback.exec(6);
        SidratCallback.exec(6);
        
        EasyMock.verify(mockedEventStore);
    }
    
}
