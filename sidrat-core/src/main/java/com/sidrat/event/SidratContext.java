package com.sidrat.event;

import com.sidrat.event.tracking.StackFrame;


public class SidratContext {
    private final StackFrame stackFrame;
    private final String threadName;
    private final long threadId;
    
    public SidratContext(StackFrame stackFrame) {
        this.stackFrame = stackFrame;
        this.threadName = Thread.currentThread().getName();
        this.threadId = Thread.currentThread().getId();
    }

    public String getClassName() {
        return stackFrame.getClassName();
    }

    public String getThreadName() {
        return threadName;
    }

    public String getMethodName() {
        return stackFrame.getMethodName();
    }

    public long getThreadId() {
        return threadId;
    }
}
