package com.sidrat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.store.hsqldb.HsqldbEventStoreFactory;
import com.sidrat.event.store.mem.InMemoryEventStoreFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BaseRecorderTest {
    @Parameters
    public static Collection<Object[]> configs() {
        List<Object[]> factories = new ArrayList<>();
        factories.add(new Object[] { new HsqldbEventStoreFactory() });
        factories.add(new Object[] { new InMemoryEventStoreFactory() });
        return factories;
    }

    private EventRepositoryFactory factory;

    public BaseRecorderTest(EventRepositoryFactory factory) {
        this.factory = factory;
    }

    @Rule
    public TestName testName;
    
    protected SidratRecorder recorder;
    protected SidratReplay replay;

    @Before
    public void setup() {
        String name = "sidrat-test";
        EventStore store = factory.store(name);
        recorder = SidratRegistry.instance().newRecorder();
        recorder.store(store);
        replay = new SidratReplay(factory.reader(name));
    }
}
