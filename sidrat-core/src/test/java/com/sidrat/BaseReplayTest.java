package com.sidrat;

import java.io.File;
import java.io.IOException;

import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.store.mem.InMemoryEventRepositoryFactory;
import com.sidrat.instrument.SidratAgent;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * Base recorder test for Sidrat testing.  Uses the InMemoryEventRepository.  Automatically instruments classes.  The
 * class must not have been loaded before starting the test.  
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public abstract class BaseReplayTest {
    private EventRepositoryFactory factory;

    public BaseReplayTest() {
        this.factory = new InMemoryEventRepositoryFactory();
    }
    
    @Rule
    public TestName testName;
    
    protected SidratRecorder recorder;
    protected SidratReplay replay;
    
    private EventStore store;
    private String repositoryName;

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
