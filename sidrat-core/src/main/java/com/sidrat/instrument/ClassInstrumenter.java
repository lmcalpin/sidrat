package com.sidrat.instrument;

import com.sidrat.SidratPermissions;
import com.sidrat.util.Logger;

import javassist.ClassPool;

/**
 * Instruments classes, taking into consideration any white and black lists to restrict our instrumentation to
 * classes in specific packages.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
class ClassInstrumenter {
    private static final Logger logger = new Logger();

    private ClassPool classPool = new ClassPool();
    private final SidratPermissions permissions;

    public ClassInstrumenter(SidratPermissions permissions) {
        this.permissions = permissions;
        classPool.appendSystemPath();
    }

    public <T> InstrumentedClass<T> instrument(Class<T> javaClass) {
        if (permissions.isAllowed(javaClass)) {
            logger.fine("Instrumenting: " + javaClass.getCanonicalName());
            return new InstrumentedClass<T>(classPool, javaClass);
        }
        return null;
    }

    public <T> InstrumentedClass<T> instrument(String className, byte[] classBytes) {
        if (permissions.isAllowed(className)) {
            logger.fine("Instrumenting: " + className);
            return new InstrumentedClass<T>(classPool, className, classBytes);
        }
        return null;
    }
}
