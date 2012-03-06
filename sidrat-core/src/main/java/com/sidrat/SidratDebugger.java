package com.sidrat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.sidrat.event.SidratClock;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.store.hsqldb.HsqldbEventStore;
import com.sidrat.event.tracking.TrackedObjects;
import com.sidrat.event.tracking.TrackedVariables;
import com.sidrat.instrument.InstrumentingClassLoader;
import com.sidrat.util.Logger;

public class SidratDebugger {
    private static final Logger logger = new Logger();
    
    private ClassLoader classLoader;
    private List<String> allowedPackages = new ArrayList<String>();

    // data trackers
    private TrackedObjects objectsTracker = new TrackedObjects();
    private TrackedVariables localVariablesTracker = new TrackedVariables();
    private SidratClock clock = new SidratClock();
    
    private EventStore eventStore;
    
    public static ThreadLocal<SidratDebugger> DEBUGGER_CONTEXT = new ThreadLocal<SidratDebugger>();
    
    public static SidratDebugger instance() {
        SidratDebugger debugger = DEBUGGER_CONTEXT.get();
        if (debugger == null) {
            debugger = new SidratDebugger();
            DEBUGGER_CONTEXT.set(debugger);
        }
        return debugger;
    }
    
    public SidratDebugger includePackage(String pkg) {
        if (pkg.startsWith("com.sidrat"))
            throw new SidratProcessingException("You can not debug Sidrat classes with Sidrat");
        allowedPackages.add(pkg);
        return this;
    }
    
    public SidratDebugger store(String filename) {
        this.eventStore = new HsqldbEventStore(filename);
        return this;
    }
    
    public SidratDebugger store(EventStore eventStore) {
        this.eventStore = eventStore;
        return this;
    }

    public void debug(String className, String... args) {
        if (this.eventStore == null) {
            store("sidrat");
        }
        DEBUGGER_CONTEXT.set(this);
        
        // include the target class' package
        includePackage(packageFromClassName(className));
        
        classLoader = new InstrumentingClassLoader(allowedPackages);
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            Class<?> clazz = classLoader.loadClass(className);
            Method method = clazz.getDeclaredMethod("main", new Class[] { String[].class });
            method.invoke(null, (Object) args);
        } catch (Exception e) {
            throw new SidratProcessingException(e);
        } finally {
            this.eventStore.close();
            this.classLoader = null;
        }
    }
    
    private String packageFromClassName(String className) {
        return className.substring(0, className.lastIndexOf('.'));
    }

    public TrackedVariables getLocalVariablesTracker() {
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
    
    public boolean allow(Class<?> clazz) {
        return allowedPackages.contains(clazz.getPackage().getName()); 
    }
}
