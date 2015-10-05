package com.sidrat.event;

import com.sidrat.SidratRegistry;
import com.sidrat.event.tracking.TrackedObject;

/**
 * Triggered when we complete execution of a method.
 * 
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratMethodExitEvent extends SidratEvent {
    private SidratMethodEntryEvent method;
    private TrackedObject returns;

    public SidratMethodExitEvent(SidratMethodEntryEvent method, TrackedObject returns) {
        this(SidratRegistry.instance().getRecorder().getClock().current(), method, returns);
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
