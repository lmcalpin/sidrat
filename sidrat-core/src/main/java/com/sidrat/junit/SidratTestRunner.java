package com.sidrat.junit;

import com.sidrat.SidratRecorder;
import com.sidrat.event.store.EventReader;
import com.sidrat.event.store.hsqldb.HsqldbEventReader;
import com.sidrat.event.store.hsqldb.HsqldbEventStore;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Instrument the class we are testing so that we can create a Sidrat recording.  If the test fails, we
 * will dump useful information, such as local variables, etc.
 *  
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratTestRunner extends BlockJUnit4ClassRunner {
    public SidratTestRunner(Class<?> klass) throws InitializationError {
        super(instrument(klass));
    }
    
    private static Class<?> instrument(Class<?> klass) {
        return SidratRecorder.instance().instrument(klass);
    }

    @Override
    public void run(RunNotifier notifier) {
        String recordingDirectory = "sidrat-junit-" + super.getTestClass().getName() + "-test";
        HsqldbEventStore store = new HsqldbEventStore(recordingDirectory);
        SidratRecorder recorder = SidratRecorder.instance();
        recorder.store(store);
        EventReader eventReader = new HsqldbEventReader(recordingDirectory);
        recorder.record(() -> {
            notifier.addListener(new SidratRunListener(recorder, eventReader));
            super.run(notifier);
        });
    }
}
