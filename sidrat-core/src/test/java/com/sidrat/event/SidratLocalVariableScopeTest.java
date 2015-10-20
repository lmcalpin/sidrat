package com.sidrat.event;

import java.util.Map;

import com.sidrat.BaseReplayTest;
import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.tracking.CapturedLocalVariableValue;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests local variable scopes are respected.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratLocalVariableScopeTest extends BaseReplayTest {
    public SidratLocalVariableScopeTest(EventRepositoryFactory factory) {
        super(factory);
    }

    /**
     * Starts a method, then calls another method. After the second method exits, we call locals on the replayer, and makes sure
     * that we can no longer see the variable from the method we just exited.
     */
    @Test
    public void testTracking() {
        SidratCallback.startRecording();
        SidratCallback.enter("threadA", "test.a", "foo", null, null);
        SidratCallback.exec(1);
        SidratCallback.variableChanged("test.a", "foo", true, "f", 1, 5);
        SidratCallback.enter("threadA", "test.b", "foo", null, null);
        SidratCallback.exec(1);
        SidratCallback.variableChanged("test.b", "foo", true, "f2", 1, 5);
        SidratCallback.exit(null);
        SidratCallback.exec(2);
        SidratCallback.exit(null);
        SidratCallback.stopRecording();

        SidratExecutionEvent lastEvent = replay.gotoEnd();
        Assert.assertEquals(3, lastEvent.getTime().intValue());
        Map<String, CapturedLocalVariableValue> vals = replay.locals(); // should only test.a.foo#f in scope
        Assert.assertEquals("f", vals.get("f").getVariable().getName());
        Assert.assertEquals(1, vals.size());
    }
}
