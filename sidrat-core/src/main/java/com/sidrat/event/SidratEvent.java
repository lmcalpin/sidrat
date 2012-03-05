package com.sidrat.event;


public abstract class SidratEvent {
    private Long time;
    
    public SidratEvent(Long time) {
        this.time = time;
    }
    
    public Long getTime() {
        return time;
    }
}
