package com.sidrat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.sidrat.event.SidratClock;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.store.hsqldb.HsqldbEventStore;
import com.sidrat.event.tracking.LocalVariables;
import com.sidrat.event.tracking.TrackedObjects;
import com.sidrat.instrument.InstrumentingClassLoader;
import com.sidrat.util.Logger;

/**
 * Records a program execution for future replay.
 * 
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratRecorder {
    private static final Logger logger = new Logger();
    
    private InstrumentingClassLoader classLoader;
    
    // packages that we allow tracking in 
    private List<String> allowedPackages = new ArrayList<String>();

    // data trackers
    private TrackedObjects objectsTracker = new TrackedObjects();
    private LocalVariables localVariablesTracker = new LocalVariables();
    private SidratClock clock = new SidratClock();
    
    private EventStore eventStore;
    
    public static ThreadLocal<SidratRecorder> RECORDER_CONTEXT = new ThreadLocal<SidratRecorder>();
    
    public static SidratRecorder instance() {
        SidratRecorder recorder = RECORDER_CONTEXT.get();
        if (recorder == null) {
            recorder = new SidratRecorder();
        }
        return recorder;
    }
    
    protected SidratRecorder() {
        this.classLoader = new InstrumentingClassLoader(allowedPackages);
        RECORDER_CONTEXT.set(this);
        Thread.currentThread().setContextClassLoader(classLoader);
    }
    
    public SidratRecorder setUseWhiteList(boolean useWhiteList) {
        this.classLoader.setUseWhiteList(useWhiteList);
        return this;
    }
    
    public SidratRecorder includePackage(String pkg) {
        if (pkg.startsWith("com.sidrat"))
            throw new SidratProcessingException("You can not record Sidrat classes with Sidrat");
        allowedPackages.add(pkg);
        return this;
    }
    
    public SidratRecorder store(String filename) {
        this.eventStore = new HsqldbEventStore(filename);
        return this;
    }
    
    public SidratRecorder store(EventStore eventStore) {
        this.eventStore = eventStore;
        return this;
    }

    private void startRecording() {
        Preconditions.checkNotNull(eventStore);
    }
    
    public void record(Runnable r) {
        try {
            startRecording();
            r.run();
        } catch (Exception e) {
            throw new SidratProcessingException(e);
        } finally {
            this.eventStore.close();
            this.classLoader = null;
        }
    }
    
    public Class<?> instrument(Class<?> clazz) {
        try {
            return classLoader.instrument(clazz);
        } catch (ClassNotFoundException e) {
            throw new SidratProcessingException("Failed to instrument " + clazz.getName(), e);
        }
    }
    
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
