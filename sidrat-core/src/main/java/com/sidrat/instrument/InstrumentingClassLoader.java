package com.sidrat.instrument;

import java.util.ArrayList;
import java.util.List;

import com.sidrat.SidratProcessingException;
import com.sidrat.util.Logger;

public class InstrumentingClassLoader extends java.lang.ClassLoader {
    private static final Logger logger = new Logger();

    private List<String> whiteList = new ArrayList<String>();

    public InstrumentingClassLoader(List<String> packages) {
        this.whiteList = packages;
    }

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

    private Class<?> instrument(Class<?> originalClass) throws ClassNotFoundException {
        InstrumentedClass instrumentedClass = InstrumentedClass.instrument(originalClass);
        Class<?> replacementClass = instrumentedClass.getReplacement();
        return replacementClass;
    }

    private boolean isAllowed(String className) {
        if (whiteList.size() == 0)
            return true;
        for (String pkg : whiteList) {
            if (className.startsWith(pkg))
                return true;
        }
        return false;
    }
}
