package com.sidrat.instrument;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class SidratAgentTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        InstrumentedClass instrumentedClass = InstrumentedClass.instrument(classBeingRedefined);
        return instrumentedClass.getReplacementBytebuffer();
    }

}
