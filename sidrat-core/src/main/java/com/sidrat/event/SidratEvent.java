package com.sidrat.event;

import java.io.PrintStream;

import com.sidrat.event.tracking.StackFrame;
import com.sidrat.event.tracking.TrackedVariable;
import com.sidrat.util.Objects;
import com.sidrat.util.Pair;

public abstract class SidratEvent {
    private Long time;
    
    public SidratEvent(Long time) {
        this.time = time;
    }
    
    public Long getTime() {
        return time;
    }
}
