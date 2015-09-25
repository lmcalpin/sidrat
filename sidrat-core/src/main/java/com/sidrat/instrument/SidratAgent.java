package com.sidrat.instrument;

import java.lang.instrument.Instrumentation;

/**
 * A Java premain agent that initializes the ClassFileTransformer that instruments classes so that Sidrat can
 * record program execution.
 * 
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratAgent {
    public static void premain(String agentArguments, Instrumentation instrumentation) {
        try {
            instrumentation.addTransformer(new SidratAgentTransformer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
