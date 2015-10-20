package com.sidrat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.store.hsqldb.HsqldbEventRepositoryFactory;
import com.sidrat.event.store.mem.InMemoryEventRepositoryFactory;
import com.sidrat.instrumentation.SidratAgent;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Base recorder test for Sidrat testing. Uses the InMemoryEventRepository. Automatically instruments classes. The
 * class must not have been loaded before starting the test.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
@RunWith(Parameterized.class)
public abstract class BaseReplayTest {
    private EventRepositoryFactory factory;

    @Rule
    public TestName testName;

    protected SidratRecorder recorder;

    protected SidratReplay replay;

    private EventStore store;
    private String repositoryName;

    public BaseReplayTest(EventRepositoryFactory factory) {
        this.factory = factory;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> configs() {
        List<Object[]> factories = new ArrayList<>();
        factories.add(new Object[] { new HsqldbEventRepositoryFactory() });
        factories.add(new Object[] { new InMemoryEventRepositoryFactory() });
        return factories;
    }

    @BeforeClass
    public static void setupSuite() {
        if (!SidratAgent.isInstrumentationAvailable()) {
            SidratRegistry.instance().getPermissions().whitelistPackage("com.metatrope.");
            SidratAgent.createAndLoadAgent();
        }
    }

    @Before
    public void setup() {
        repositoryName = "sidrat-testrepo-" + System.currentTimeMillis();
        store = factory.store(repositoryName);
        recorder = SidratRegistry.instance().newRecorder();
        recorder.store(store);
        replay = new SidratReplay(factory.reader(repositoryName));
    }

    @After
    public void tearDown() throws IOException {
        store.close();
        File file = new File(repositoryName);
        if (file.exists()) {
            FileUtils.deleteDirectory(file);
        }

    }
}
