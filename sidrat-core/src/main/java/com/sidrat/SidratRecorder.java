package com.sidrat;

import java.lang.reflect.Method;

import com.google.common.base.Preconditions;
import com.sidrat.event.SidratClock;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.store.mem.InMemoryEventStore;
import com.sidrat.event.tracking.LocalVariables;
import com.sidrat.event.tracking.TrackedObjects;
import com.sidrat.instrument.InstrumentingClassLoader;

/**
 * Records a program execution for future replay.
 * 
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratRecorder {
    private InstrumentingClassLoader classLoader;
    
    // data trackers
    private TrackedObjects objectsTracker = new TrackedObjects();
    private LocalVariables localVariablesTracker = new LocalVariables();
    private SidratClock clock = new SidratClock();
    
    private EventStore eventStore;
    
    protected SidratRecorder() {
        createClassLoader();
    }

    private void createClassLoader() {
        this.classLoader = new InstrumentingClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
    }
    
    public SidratRecorder setUseWhiteList(boolean useWhiteList) {
        this.classLoader.setUseWhiteList(useWhiteList);
        return this;
    }
    
    public SidratRecorder includePackage(String pkg) {
        if (pkg.startsWith("com.sidrat"))
            throw new SidratProcessingException("You can not record Sidrat classes with Sidrat");
        classLoader.addToWhiteList(pkg);
        return this;
    }
    
    public SidratRecorder store(String filename) {
        this.eventStore = new InMemoryEventStore(true, filename);
        return this;
    }
    
    public SidratRecorder store(EventStore eventStore) {
        this.eventStore = eventStore;
        return this;
    }

    private void startRecording() {
        Preconditions.checkNotNull(eventStore);
        createClassLoader();
    }
    
    /**
     * Start recording while executing a Runnable, stopping after the Runnable returns.
     */
    public void record(Runnable r) {
        try {
            startRecording();
            r.run();
        } catch (Exception e) {
            throw new SidratProcessingException(e);
        } finally {
            stop();
        }
    }
    
    /**
     * Start recording
     */
    public void record(String className, String... args) {
        record(() -> {
            try {
                includePackage(packageFromClassName(className));
                Class<?> clazz = classLoader.loadClass(className);
                Method method = clazz.getDeclaredMethod("main", new Class[] { String[].class });
                method.invoke(null, (Object)args);
            } catch (Exception e) {
                throw new SidratProcessingException("Failed to execute main method on " + className, e);
            }
        });
    }
    
    /**
     * Stop recording
     */
    public void stop() {
        if (this.eventStore != null)
            this.eventStore.close();
        this.objectsTracker = new TrackedObjects();
        this.localVariablesTracker = new LocalVariables();
        this.clock = new SidratClock();
    }
    
    public Class<?> instrument(Class<?> clazz) {
        try {
            return classLoader.instrument(clazz);
        } catch (ClassNotFoundException e) {
            throw new SidratProcessingException("Failed to instrument " + clazz.getName(), e);
        }
    }
    
    private String packageFromClassName(String className) {
        return className.substring(0, className.lastIndexOf('.'));
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
