package com.sidrat.event;

import java.io.PrintStream;

import com.sidrat.SidratDebugger;
import com.sidrat.event.tracking.ExecutionLocation;
import com.sidrat.event.tracking.TrackedObject;

public class SidratMethodEntryEvent extends SidratEvent {
    private ExecutionLocation executionContext;
    private Long threadID;
    private String threadName;

    public SidratMethodEntryEvent(ExecutionLocation executionContext) {
        this(SidratDebugger.instance().getClock().next(), executionContext, Thread.currentThread().getId(), Thread.currentThread().getName());
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
