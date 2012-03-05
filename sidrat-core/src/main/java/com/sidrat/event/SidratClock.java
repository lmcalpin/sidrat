package com.sidrat.event;

import java.util.concurrent.atomic.AtomicLong;

public class SidratClock {
    private final AtomicLong time;
    
    public static SidratClock CLOCK = new SidratClock();
    
    public static SidratClock instance() {
        return CLOCK;
    }
    
    private SidratClock() {
        time = new AtomicLong(0);
    }

    public long next() {
        return time.incrementAndGet();
    }
    
    public long current() {
        return time.get();
    }
    
    public void reset() {
        time.set(0L);
    }
}
