package com.sidrat.instrument;

import java.io.IOException;

import com.sidrat.SidratProcessingException;
import com.sidrat.util.Logger;

import javassist.ByteArrayClassPath;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * A class that has been instrumented by Sidrat to invoke callbacks whenever we begin or end execution of a method, or
 * alter the state of a field or local variable.
 * 
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
class InstrumentedClass<T> {
    private static final Logger logger = new Logger();

    private CtClass ctClass;

    public InstrumentedClass(ClassPool pool, Class<T> clazz) {
        createReplacement(pool, clazz);
    }

    public InstrumentedClass(ClassPool pool, String className, byte[] bytes) {
        pool.insertClassPath(new ByteArrayClassPath(className, bytes));
        createReplacement(pool, className, bytes);
    }

    private void createReplacement(ClassPool pool, Class<T> original) {
        String className = original.getName();
        
        try {
            ctClass = pool.get(className);
        } catch (NotFoundException e1) {
            throw new SidratProcessingException("Could not locate: " + className);
        }
        
        try {
            for (CtClass intf : ctClass.getInterfaces()) {
                if (intf.getName().equalsIgnoreCase(Instrumented.class.getName())) {
                    return;
                }
            }
        } catch (NotFoundException e) {
            throw new SidratProcessingException("Failed while examining: " + className);
        }
        
        instrument(pool, className, ctClass);
    }

    private byte[] createReplacement(ClassPool pool, String className, byte[] bytes) {
        try {
            ctClass = pool.get(className);
            instrument(pool, className, ctClass);
            return getReplacementBytebuffer();
        } catch (Exception e) {
            throw new SidratProcessingException("Failed while processing: " + className, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public Class<T> toClass() {
        try {
            Class<T> replacement = ctClass.toClass();
            return replacement;
        } catch (CannotCompileException e) {
            throw new SidratProcessingException("Error enhancing: " + ctClass.getName(), e);
        }
    }

    private void instrument(ClassPool pool, String className, CtClass ctClass) {
        try {
            ctClass.addInterface(pool.get(Instrumented.class.getName()));
        } catch (NotFoundException e1) {
            throw new SidratProcessingException("Failed to add marker interface to " + className);
        }
        
        for (final CtBehavior ctBehavior : ctClass.getDeclaredBehaviors()) {
            MethodInstrumenter methodInstrumenter = new MethodInstrumenter(ctBehavior);
            methodInstrumenter.instrument();
        }
    }

    public byte[] getReplacementBytebuffer() {
        try {
            return ctClass.toBytecode();
        } catch (IOException | CannotCompileException e) {
            throw new SidratProcessingException("Error creating bytecode: " + ctClass.getName(), e);
        }
    }
}
