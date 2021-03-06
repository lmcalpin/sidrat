package com.sidrat.instrumentation;

import com.sidrat.SidratPermissions;
import com.sidrat.SidratProcessingException;
import com.sidrat.SidratRegistry;
import com.sidrat.util.Logger;

/**
 * A classloader that instruments the classes it loads so that we can record program execution.
 * 
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class InstrumentingClassLoader extends java.lang.ClassLoader {
    private static final Logger logger = new Logger();

    private final ClassInstrumenter instrumenter;
    private final SidratPermissions permissions;

    public InstrumentingClassLoader() {
        this(SidratRegistry.instance().getPermissions());
    }

    public InstrumentingClassLoader(SidratPermissions permissions) {
        this.permissions = permissions;
        this.instrumenter = new ClassInstrumenter(permissions);
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws java.lang.ClassNotFoundException {
        Class<?> clazz = findLoadedClass(className);
        if (clazz != null)
            return clazz;

        Class<?> originalClass = getParent().loadClass(className);
        if (!permissions.isAllowed(className)) {
            return originalClass;
        }
        try {
            clazz = instrument(originalClass);
        } catch (ClassNotFoundException e) {
            throw new SidratProcessingException("Error instrumenting " + className, e);
        }

        if (clazz == null)
            throw new java.lang.ClassNotFoundException(className);

        if (resolve)
            resolveClass(clazz);

        return clazz;
    }

    public Class<?> instrument(Class<?> originalClass) throws ClassNotFoundException {
        try {
            InstrumentedClass<?> instrumentedClass = instrumenter.instrument(originalClass);
            if (instrumentedClass == null)
                return originalClass;
            Class<?> replacementClass = instrumentedClass.toClass();
            return replacementClass;
        } catch (ClassInstrumentationException e) {
            return originalClass;
        }
    }
}
