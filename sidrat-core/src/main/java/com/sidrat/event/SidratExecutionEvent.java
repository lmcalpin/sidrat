package com.sidrat.event;

import java.io.PrintStream;

import com.sidrat.SidratRecorder;
import com.sidrat.event.tracking.ExecutionLocation;
import com.sidrat.event.tracking.TrackedObject;

public class SidratExecutionEvent extends SidratEvent {
    private SidratMethodEntryEvent method;
    private int lineNumber;

    public SidratExecutionEvent(SidratMethodEntryEvent method, int lineNumber) {
        this(SidratRecorder.instance().getClock().next(), method, lineNumber);
    }

    public SidratExecutionEvent(Long time, SidratMethodEntryEvent method, int lineNumber) {
        super(time);
        this.method = method;
        this.lineNumber = lineNumber;
    }

    public static SidratExecutionEvent exec(Long time, SidratMethodEntryEvent method, int lineNumber) {
        SidratExecutionEvent event = new SidratExecutionEvent(time, method, lineNumber);
        return event;
    }

    public ExecutionLocation getExecutionContext() {
        return method.getExecutionContext();
    }

    public int getLineNumber() {
        return lineNumber;
    }
    
    public String getClassName() {
        return method.getClassName();
    }

    public String getMethodName() {
        return method.getMethodName();
    }
    
    public Long getMethodEntryTime() {
        return method.getTime();
    }

    public void print(PrintStream stream) {
        stream.println("[" + getTime() + "] " + method.getClassName() + "." + method.getMethodName() + ":" + getLineNumber());
    }

    public String asBreakpointID() {
        String className = method.getClassName();
        if (className == null)
            return null;
        return className + ":" + this.lineNumber;
    }
}
