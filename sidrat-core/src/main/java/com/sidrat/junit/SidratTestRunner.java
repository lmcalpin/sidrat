package com.sidrat.junit;

import com.sidrat.SidratRecorder;
import com.sidrat.SidratRegistry;
import com.sidrat.event.store.EventReader;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.store.hsqldb.HsqldbEventReader;
import com.sidrat.event.store.hsqldb.HsqldbEventStore;
import com.sidrat.event.store.mem.InMemoryEventReader;
import com.sidrat.event.store.mem.InMemoryEventStore;

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
        return SidratRegistry.instance().getRecorder().instrument(klass);
    }

    @Override
    public void run(RunNotifier notifier) {
        EventStore store = null;
        EventReader reader = null;
        SidratRecorder recorder = SidratRegistry.instance().getRecorder();
        SidratEventStore ann = super.getTestClass().getJavaClass().getAnnotation(SidratEventStore.class);
        if (ann != null) {
            String factoryClass = ann.factory();
            String name = ann.name();
            try {
                EventRepositoryFactory eventStoreFactory = (EventRepositoryFactory) Class.forName(factoryClass).newInstance();
                store = eventStoreFactory.store(name);
                reader = eventStoreFactory.reader(name);
            } catch (Exception e) {
                throw new IllegalStateException("A Sidrat Event Store could not be initialized");
            }
        }
        // default values
        if (store == null) {
            InMemoryEventStore memstore = new InMemoryEventStore();
            store = memstore;
            reader = new InMemoryEventReader(memstore);
        }
        recorder.store(store);
        final EventReader eventReader = reader; 
        recorder.record(() -> {
            notifier.addListener(new SidratRunListener(recorder, eventReader));
            super.run(notifier);
        });
    }
}
