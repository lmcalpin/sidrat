package com.sidrat.instrument;

import java.util.ArrayList;
import java.util.List;

import javassist.ClassPool;

import com.sidrat.SidratProcessingException;
import com.sidrat.util.Logger;

/**
 * A classloader that instruments the classes it loads so that we can record program execution.
 * 
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class InstrumentingClassLoader extends java.lang.ClassLoader {
    private static final Logger logger = new Logger();

    private List<String> whiteList = new ArrayList<String>();
    private boolean useWhiteList = true;
    
    private ClassPool pool = new ClassPool();

    public InstrumentingClassLoader(List<String> packages) {
        this.whiteList = packages;
        this.pool.appendSystemPath();
    }
    
    public void setUseWhiteList(boolean useWhiteList) {
        this.useWhiteList = useWhiteList;
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws java.lang.ClassNotFoundException {
        Class<?> clazz = findLoadedClass(className);
        if (clazz != null)
            return clazz;

        Class<?> originalClass = getParent().loadClass(className);
        if (!isAllowed(className)) {
            return originalClass;
        }
        try {
            logger.info("Instrumenting: " + className);
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
        InstrumentedClass instrumentedClass = InstrumentedClass.instrument(pool, originalClass);
        Class<?> replacementClass = instrumentedClass.getReplacement();
        return replacementClass;
    }

    private boolean isAllowed(String className) {
        if (!useWhiteList)
            return true;
        for (String pkg : whiteList) {
            if (className.startsWith(pkg))
                return true;
        }
        return false;
    }
}
