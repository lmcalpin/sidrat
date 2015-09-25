package com.sidrat.instrument;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;

import com.sidrat.util.Logger;

/**
 * A ClassFileTransformer that alters classes that it loads to enable recording of program execution.
 * 
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratAgentTransformer implements ClassFileTransformer {
    private static final Logger logger = new Logger();
    
    private ClassInstrumenter instrumenter = new ClassInstrumenter();
    private static boolean isAgentActive = false;

    public SidratAgentTransformer() {
        String whiteList = System.getProperty("com.sidrat.agent.package.whitelist");
        if (whiteList != null) {
            String[] packages = whiteList.split(",");
            Arrays.stream(packages).forEach(p -> instrumenter.whitelistPackage(p));
        }
        isAgentActive = true;
    }
    
    public static boolean isActive() { return isAgentActive; }

    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        String adjustedClassName = className.replace("/", ".");
        if (instrumenter.isAllowed(adjustedClassName)) {
            try {
                InstrumentedClass<?> instrumentedClass = instrumenter.instrument(adjustedClassName, classfileBuffer);
                if (instrumentedClass == null) {
                    return classfileBuffer; 
                }
                return instrumentedClass.getReplacementBytebuffer();
            } catch (Exception e) {
                e.printStackTrace();
                return classfileBuffer;
            }
        }
        return null;
    }
}
