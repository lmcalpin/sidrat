package com.sidrat.event;

import java.io.PrintStream;

import com.sidrat.SidratDebugger;
import com.sidrat.event.tracking.StackFrame;
import com.sidrat.event.tracking.TrackedObject;

public class SidratExecutionEvent extends SidratEvent {
    private TrackedObject executionContext;
    private String className;
    private String methodName;
    private String threadName;
    private Long threadID;
    private int lineNumber;
    private boolean entering;

    public SidratExecutionEvent(TrackedObject executionContext, int lineNumber, boolean entering) {
        this(SidratDebugger.instance().getClock().next(), executionContext, SidratCallback.currentFrame(), Thread.currentThread().getId(), Thread.currentThread().getName(), lineNumber, entering);
    }

    public SidratExecutionEvent(Long time, TrackedObject executionContext, StackFrame stackFrame, Long threadID, String threadName, int lineNumber, boolean entering) {
        super(time);
        if (stackFrame != null) {
            this.className = stackFrame.getClassName();
            this.methodName = stackFrame.getMethodName();
        }
        this.executionContext = executionContext;
        this.threadName = threadName;
        this.threadID = threadID;
        this.lineNumber = lineNumber;
        this.entering = entering;
    }

    public static SidratExecutionEvent exec(TrackedObject executionContext, int lineNumber, boolean entering) {
        SidratExecutionEvent event = new SidratExecutionEvent(executionContext, lineNumber, entering);
        return event;
    }

    public boolean isEntering() {
        return entering;
    }

    public TrackedObject getExecutionContext() {
        return executionContext;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getThreadName() {
        return threadName;
    }

    public Long getThreadID() {
        return threadID;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void print(PrintStream stream) {
        stream.println("[" + getTime() + "] " + getClassName() + "." + getMethodName() + ":" + getLineNumber());
    }

    public String asBreakpointID() {
        if (className == null)
            return null;
        return className + ":" + this.lineNumber;
    }
}
