package com.sidrat.instrument;

import java.io.IOException;

import com.sidrat.SidratProcessingException;
import com.sidrat.util.Logger;

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
public class InstrumentedClass {
    private final String className, packageName;
    private Class<?> original;
    private Class<?> replacement;
    private ClassPool pool;
    
    private static final Logger logger = new Logger();

    private CtClass ctClass;

    public InstrumentedClass(ClassPool pool, Class<?> clazz) {
        this.pool = pool;
        this.original = clazz;
        this.className = clazz.getName();
        this.packageName = clazz.getPackage().getName();
        this.replacement = createReplacement(original);
    }

    public static InstrumentedClass instrument(ClassPool pool, Class<?> javaClass) {
        return new InstrumentedClass(pool, javaClass);
    }

    private Class<?> createReplacement(Class<?> original) {
        try {
            ctClass = pool.get(className);
        } catch (NotFoundException e1) {
            throw new SidratProcessingException("Could not locate: " + className);
        }
        
        try {
            for (CtClass intf : ctClass.getInterfaces()) {
                if (intf.getName().equalsIgnoreCase(Instrumented.class.getName())) {
                    throw new SidratProcessingException("Already instrumented: " + className);
                }
            }
        } catch (NotFoundException e) {
            throw new SidratProcessingException("Failed while examining: " + className);
        }

        try {
            ctClass.addInterface(pool.get(Instrumented.class.getName()));
        } catch (NotFoundException e1) {
            throw new SidratProcessingException("Failed to add marker interface to " + className);
        }
        
        for (final CtBehavior ctBehavior : ctClass.getDeclaredBehaviors()) {
            MethodInstrumenter methodInstrumenter = new MethodInstrumenter(ctBehavior);
            methodInstrumenter.instrument();
        }
        
        Class<?> replacement;
        try {
            replacement = ctClass.toClass();
        } catch (CannotCompileException e) {
            throw new SidratProcessingException("Error enhancing: " + className, e);
        }
        ctClass.defrost();
        return replacement;
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public Class<?> getOriginal() {
        return original;
    }

    public Class<?> getReplacement() {
        return replacement;
    }

    public byte[] getReplacementBytebuffer() {
        try {
            return ctClass.toBytecode();
        } catch (IOException e) {
            throw new SidratProcessingException("Error enhancing: " + className, e);
        } catch (CannotCompileException e) {
            throw new SidratProcessingException("Error enhancing: " + className, e);
        }
    }
}
