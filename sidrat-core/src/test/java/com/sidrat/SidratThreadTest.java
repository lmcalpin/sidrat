package com.sidrat;

import com.metatrope.testprogram.ForThreadTest;

import com.sidrat.event.store.EventRepositoryFactory;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("TODO")
public class SidratThreadTest extends BaseRecorderTest {
    public SidratThreadTest(EventRepositoryFactory factory) {
        super(factory);
    }

    // verify we do not have a regression where the beginning of the for loop generates two events
    @Test
    public void testLocalVariableTracking() {
        recorder.record(ForThreadTest.class.getName());
        replay.withSource("src/test/java");
    }
}
