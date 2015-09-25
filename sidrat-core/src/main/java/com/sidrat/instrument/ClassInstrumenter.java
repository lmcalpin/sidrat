package com.sidrat.instrument;

import java.util.ArrayList;
import java.util.List;

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

    private List<String> packageWhitelist = new ArrayList<>();
    private List<String> packageBlacklist = new ArrayList<>();
    private List<String> classWhitelist = new ArrayList<>();
    private boolean whitelistEverything = false;

    private ClassPool classPool = new ClassPool();

    public ClassInstrumenter() {
        classPool.appendSystemPath();
    }

    /**
     * Do not use a whitelist; attempt to instrument all classes.
     * 
     * @return
     */
    public ClassInstrumenter allowAll() {
        whitelistEverything = true;
        return this;
    }

    public ClassInstrumenter whitelistPackage(String packageRoot) {
        this.packageWhitelist.add(packageRoot);
        return this;
    }

    public ClassInstrumenter whitelistClass(String className) {
        this.classWhitelist.add(className);
        return this;
    }

    public ClassInstrumenter blacklistPackage(String packageRoot) {
        this.packageBlacklist.add(packageRoot);
        return this;
    }

    public <T> InstrumentedClass<T> instrument(Class<T> javaClass) {
        if (isAllowed(javaClass)) {
            logger.fine("Instrumenting: " + javaClass.getCanonicalName());
            return new InstrumentedClass<T>(classPool, javaClass);
        }
        return null;
    }

    public <T> InstrumentedClass<T> instrument(String className, byte[] classBytes) {
        if (isAllowed(className)) {
            logger.fine("Instrumenting: " + className);
            return new InstrumentedClass<T>(classPool, className, classBytes);
        }
        return null;
    }

    public boolean isAllowed(Class<?> javaClass) {
        String classname = javaClass.getName();
        return isAllowed(classname);
    }

    public boolean isAllowed(String classname) {
        if (whitelistEverything) {
            if (classWhitelist.contains(classname))
                return true;
            return !onPackageBlacklist(classname);
        }
        if (classWhitelist.contains(classname))
            return true;
        for (String packageRoot : packageWhitelist) {
            if (classname.startsWith(packageRoot + ".")) {
                return !onPackageBlacklist(classname);
            }
        }
        return false;
    }

    private boolean onPackageBlacklist(String classname) {
        if (classname.startsWith("com.sidrat."))
            return true;
        if (classname.startsWith("java."))
            return true;
        if (classname.startsWith("org.junit."))
            return true;
        for (String packageRoot : packageBlacklist) {
            if (classname.startsWith(packageRoot + ".")) {
                return true;
            }
        }
        return false;
    }

}
