package com.sidrat.instrument;

import java.io.IOException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;

import com.sidrat.SidratProcessingException;
import com.sidrat.util.Logger;

public class InstrumentedClass {
    private final String className, packageName;
    private Class<?> original;
    private Class<?> replacement;
    private static ClassPool pool = ClassPool.getDefault();

    private static final Logger logger = new Logger();

    private CtClass ctClass;

    public InstrumentedClass(Class<?> clazz) {
        this.original = clazz;
        this.className = clazz.getName();
        this.packageName = clazz.getPackage().getName();
        this.replacement = createReplacement(original);
    }

    public static InstrumentedClass instrument(Class<?> javaClass) {
        return new InstrumentedClass(javaClass);
    }

    private Class<?> createReplacement(Class<?> original) {
        try {
            ctClass = pool.get(className);
        } catch (NotFoundException e1) {
            throw new SidratProcessingException("Could not locate: " + className);
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
