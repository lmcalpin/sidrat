package com.sidrat.event;

import java.util.Map;

import com.sidrat.SidratRegistry;
import com.sidrat.event.tracking.ExecutionLocation;

/**
 * Triggered when we start execution of a new method.
 *  
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratMethodEntryEvent extends SidratEvent {
    private ExecutionLocation executionContext;
    private Long threadID;
    private String threadName;
    private Map<String,Object> arguments;
    
    public SidratMethodEntryEvent(ExecutionLocation executionContext, Map<String,Object> arguments) {
        this(SidratRegistry.instance().getRecorder().getClock().next(), executionContext, Thread.currentThread().getId(), Thread.currentThread().getName(), arguments);
    }

    public SidratMethodEntryEvent(Long time, ExecutionLocation executionContext, Long threadID, String threadName, Map<String,Object> arguments) {
        super(time);
        this.executionContext = executionContext;
        this.threadID = threadID;
        this.threadName = threadName;
        this.arguments = arguments;
    }

    public static SidratMethodEntryEvent entering(ExecutionLocation executionContext, Map<String,Object> arguments) {
        SidratMethodEntryEvent event = new SidratMethodEntryEvent(executionContext, arguments);
        return event;
    }

    public ExecutionLocation getExecutionContext() {
        return executionContext;
    }

    public Long getThreadID() {
        return threadID;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getClassName() {
        return executionContext.getClassName();
    }

    public String getMethodName() {
        return executionContext.getMethodName();
    }

}
