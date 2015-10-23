package com.metatrope.sidrat.jpa;

import com.sidrat.SidratRecorder;
import com.sidrat.SidratRegistry;
import com.sidrat.SidratReplay;
import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.store.jpa.JPAEventRepositoryFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class JPAReplayTest {
    protected SidratRecorder recorder;
    protected SidratReplay replay;
    private EventStore store;
    private String repositoryName;

    @Before
    public void setup() {
        JPAEventRepositoryFactory factory = new JPAEventRepositoryFactory();
        repositoryName = "sidrat-testrepo-" + System.currentTimeMillis();
        store = factory.store(repositoryName);
        recorder = SidratRegistry.instance().newRecorder();
        recorder.store(store);
        replay = new SidratReplay(factory.reader(repositoryName));
    }

    @Test
    public void testLookupSourceCode() {
        recorder.record(() -> {
            int sum = 0;
            for (int i = 0; i < 5; i++) {
                sum += 5;
            }
            int finalValue = sum;
            System.out.println(finalValue);
        });
        SidratExecutionEvent event = replay.gotoEvent(1);
        Assert.assertEquals(1, replay.locals().size());
        replay.gotoEnd();
        Assert.assertEquals(2, replay.locals().size());
    }
}
