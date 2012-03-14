package com.sidrat.event;

import com.sidrat.SidratDebugger;
import com.sidrat.event.tracking.TrackedObject;

public class SidratMethodExitEvent extends SidratEvent {
    private SidratMethodEntryEvent method;
    private TrackedObject returns;

    public SidratMethodExitEvent(SidratMethodEntryEvent method, TrackedObject returns) {
        this(SidratDebugger.instance().getClock().current(), method, returns);
    }

    public SidratMethodExitEvent(Long time, SidratMethodEntryEvent method, TrackedObject returns) {
        super(time);
        this.method = method;
    }

    public static SidratMethodExitEvent exiting(SidratMethodEntryEvent method, TrackedObject returns) {
        SidratMethodExitEvent event = new SidratMethodExitEvent(method, returns);
        return event;
    }
    
    public Long getMethodEntryTime() {
        return method.getTime();
    }

    public TrackedObject getReturns() {
        return returns;
    }

}
