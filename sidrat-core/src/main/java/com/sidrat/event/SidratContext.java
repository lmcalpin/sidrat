package com.sidrat.event;

import com.sidrat.event.tracking.ExecutionLocation;

public class SidratContext {
    private final ExecutionLocation stackFrame;
    private final String threadName;
    private final long threadId;

    public SidratContext(ExecutionLocation stackFrame) {
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
