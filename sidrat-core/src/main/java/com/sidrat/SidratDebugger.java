package com.sidrat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.sidrat.event.SidratClock;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.store.hsqldb.HsqldbEventStore;
import com.sidrat.event.tracking.LocalVariables;
import com.sidrat.instrument.InstrumentingClassLoader;
import com.sidrat.util.Logger;

public class SidratDebugger {
    private static final Logger logger = new Logger();
    
    private ClassLoader classLoader;
    private List<String> allowedPackages = new ArrayList<String>();

    // data trackers
    private LocalVariables localVariablesTracker = new LocalVariables();
    
    private EventStore eventStore;
    
    public static ThreadLocal<SidratDebugger> DEBUGGER_CONTEXT = new ThreadLocal<SidratDebugger>();
    
    public static SidratDebugger instance() {
        return DEBUGGER_CONTEXT.get();
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
        SidratClock.instance().reset();
        
        // include the target classe's package
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
        }
    }
    
    private String packageFromClassName(String className) {
        return className.substring(0, className.lastIndexOf('.'));
    }

    public LocalVariables getLocalVariablesTracker() {
        return localVariablesTracker;
    }

    public EventStore getEventStore() {
        return eventStore;
    }
}
