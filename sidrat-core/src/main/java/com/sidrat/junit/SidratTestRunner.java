package com.sidrat.junit;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.sidrat.SidratRecorder;
import com.sidrat.SidratRegistry;
import com.sidrat.event.SidratCallback;
import com.sidrat.event.store.EventReader;
import com.sidrat.event.store.EventRepositoryFactory;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.store.mem.InMemoryEventRepository;
import com.sidrat.instrumentation.ClassInstrumentationException;
import com.sidrat.instrumentation.Instrumented;
import com.sidrat.instrumentation.InstrumentingClassLoader;
import com.sidrat.instrumentation.SidratAgentTransformer;

import org.junit.Assert;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Instrument the class we are testing so that we can create a Sidrat recording. If the test fails, we
 * will dump useful information, such as local variables, etc.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratTestRunner extends BlockJUnit4ClassRunner {
    public SidratTestRunner(Class<?> klass) throws InitializationError {
        super(instrument(klass));
    }

    private static Class<?> instrument(Class<?> klass) {
        try {
            // don't instrument if the agent is running
            if (SidratAgentTransformer.isActive()) {
                Assert.assertTrue("TestRunner expected a Sidrat instrumented test class", instrumented(klass));
                return klass;
            }
            InstrumentingClassLoader classLoader = new InstrumentingClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            return classLoader.instrument(klass);
        } catch (ClassInstrumentationException e) {
            return klass;
        } catch (ClassNotFoundException e) {
            return klass;
        }
    }

    private static boolean instrumented(Class<?> klass) {
        for (Class<?> intf : klass.getInterfaces()) {
            if (intf.isAssignableFrom(Instrumented.class)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run(RunNotifier notifier) {
        EventStore store = null;
        EventReader reader = null;
        SidratRecorder recorder = SidratRegistry.instance().getRecorder();
        SidratEventStore ann = super.getTestClass().getJavaClass().getAnnotation(SidratEventStore.class);
        if (ann != null) {
            Class<?> factoryClass = ann.factory();
            String name = ann.name();
            try {
                EventRepositoryFactory eventStoreFactory = (EventRepositoryFactory) factoryClass.newInstance();
                store = eventStoreFactory.store(name);
                reader = eventStoreFactory.reader(name);
            } catch (Exception e) {
                throw new IllegalStateException("A Sidrat Event Store could not be initialized", e);
            }
        }
        SidratHistory historyAnn = super.getTestClass().getJavaClass().getAnnotation(SidratHistory.class);
        List<String> historyVars = historyAnn != null ? Lists.newArrayList(historyAnn.variables()) : Collections.emptyList();
        // default values
        if (store == null) {
            InMemoryEventRepository memstore = new InMemoryEventRepository();
            store = memstore;
            reader = memstore;
        }
        recorder.store(store);
        final EventReader eventReader = reader;
        notifier.addListener(new SidratRunListener(recorder, eventReader, historyVars));
        try {
            SidratCallback.startRecording();
            super.run(notifier);
        } finally {
            SidratCallback.stopRecording();
        }
    }
}
