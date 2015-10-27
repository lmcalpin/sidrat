package com.sidrat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.store.hsqldb.HsqldbEventRepositoryFactory;
import com.sidrat.event.tracking.CapturedLocalVariableValue;
import com.sidrat.instrumentation.SidratAgent;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Base recorder test for Sidrat testing. Since we black list anything in com.sidrat from instrumentation, these
 * tests are in another package.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
@RunWith(Parameterized.class)
public abstract class BaseRecorderTest {
    private EventRepositoryFactory factory;

    @Rule
    public TestName testName;

    protected SidratRecorder recorder;

    protected SidratReplay replay;

    private EventStore store;
    private String repositoryName;

    public BaseRecorderTest(EventRepositoryFactory factory) {
        this.factory = factory;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        List<Object[]> factories = new ArrayList<>();
        factories.add(new Object[] { new HsqldbEventRepositoryFactory() });
        // factories.add(new Object[] { new InMemoryEventRepositoryFactory() });
        // factories.add(new Object[] { new JPAEventRepositoryFactory() });
        return factories;
    }

    /**
     * Execute each line and dump the local variables to stdout
     */
    public void dumpLocals() {
        SidratExecutionEvent event = replay.gotoEvent(1);
        int i = 0;
        while (event != null) {
            i++;
            System.out.println(i + ": " + event.getLineNumber());
            Map<String, CapturedLocalVariableValue> locals = replay.locals();
            System.out.println(locals);
            event = replay.readNext();
        }
    }

    @Before
    public void setup() {
        if (!SidratAgent.isInstrumentationAvailable())
            Assert.fail("Sidrat agent is not running");
        repositoryName = "sidrat-testrepo-" + System.currentTimeMillis();
        store = factory.store(repositoryName);
        recorder = SidratRegistry.instance().newRecorder();
        recorder.store(store);
        replay = new SidratReplay(factory.reader(repositoryName));
    }

    @After
    public void tearDown() throws IOException {
        if (store != null)
            store.close();
        File file = new File(repositoryName);
        if (file.exists()) {
            FileUtils.deleteDirectory(file);
        }

    }
}
