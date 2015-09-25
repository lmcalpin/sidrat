package com.sidrat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.google.common.base.Preconditions;
import com.sidrat.event.SidratClock;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.store.hsqldb.HsqldbEventStore;
import com.sidrat.event.tracking.LocalVariables;
import com.sidrat.event.tracking.TrackedObjects;
import com.sidrat.instrument.InstrumentingClassLoader;

/**
 * Records a program execution for future replay.
 * 
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratRecorder {
    // data trackers
    private TrackedObjects objectsTracker = new TrackedObjects();
    private LocalVariables localVariablesTracker = new LocalVariables();
    private SidratClock clock = new SidratClock();

    private EventStore eventStore;

    protected SidratRecorder() {
    }

    public SidratRecorder store(String filename) {
        this.eventStore = new HsqldbEventStore(filename);
        return this;
    }

    public SidratRecorder store(EventStore eventStore) {
        this.eventStore = eventStore;
        return this;
    }

    public void reset() {
        Preconditions.checkNotNull(eventStore);
        this.objectsTracker = new TrackedObjects();
        this.localVariablesTracker = new LocalVariables();
        this.clock = new SidratClock();
    }

    /**
     * Start recording
     */
    public void record(String className, String... args) {
        reset();
        InstrumentingClassLoader classLoader = new InstrumentingClassLoader();
        classLoader.whitelistClass(className);
        Thread.currentThread().setContextClassLoader(classLoader);
        Class<?> clazz;
        try {
            clazz = classLoader.loadClass(className);
            Method method = clazz.getDeclaredMethod("main", new Class[] { String[].class });
            if (Modifier.isStatic(method.getModifiers())) {
                method.invoke(null, (Object) args);
            } else {
                throw new SidratProcessingException("Missing main method on: " + className);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new SidratProcessingException("Failed to execute main method on: " + className);
        }
    }

    public void record(Runnable r) {
        reset();
        ClassLoader classLoader = new InstrumentingClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        r.run();
    }

    /**
     * Stop recording
     */
    public void stop() {
        if (this.eventStore != null)
            this.eventStore.close();
    }

    public LocalVariables getLocalVariablesTracker() {
        return localVariablesTracker;
    }

    public TrackedObjects getObjectTracker() {
        return objectsTracker;
    }

    public EventStore getEventStore() {
        return eventStore;
    }

    public SidratClock getClock() {
        return clock;
    }
}
