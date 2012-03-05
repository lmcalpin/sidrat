package com.sidrat;

import java.lang.instrument.Instrumentation;

import com.sidrat.instrument.SidratAgentTransformer;

public class SidratAgent {
    public static void premain(String agentArguments, Instrumentation instrumentation) {
        instrumentation.addTransformer(new SidratAgentTransformer());
    }
}
