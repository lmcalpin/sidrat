package com.sidrat.event;

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
    
    public SidratMethodEntryEvent(ExecutionLocation executionContext) {
        this(SidratRegistry.instance().getRecorder().getClock().next(), executionContext, Thread.currentThread().getId(), Thread.currentThread().getName());
    }

    public SidratMethodEntryEvent(Long time, ExecutionLocation executionContext, Long threadID, String threadName) {
        super(time);
        this.executionContext = executionContext;
        this.threadID = threadID;
        this.threadName = threadName;
    }

    public static SidratMethodEntryEvent entering(ExecutionLocation executionContext) {
        SidratMethodEntryEvent event = new SidratMethodEntryEvent(executionContext);
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
