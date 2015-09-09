package com.sidrat.instrument;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ClassPool;

/**
 * A ClassFileTransformer that alters classes that it loads to enable recording of program execution.
 *  
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratAgentTransformer implements ClassFileTransformer {
    private static ClassPool pool = ClassPool.getDefault();

    static {
        pool.appendSystemPath();
    }
    
    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (classBeingRedefined == null)
            return classfileBuffer;
        InstrumentedClass instrumentedClass = InstrumentedClass.instrument(pool, classBeingRedefined);
        return instrumentedClass.getReplacementBytebuffer();
    }

}
