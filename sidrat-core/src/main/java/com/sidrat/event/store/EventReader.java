package com.sidrat.event.store;

import java.util.List;
import java.util.Map;

import com.sidrat.event.SidratEvent;
import com.sidrat.event.SidratExecutionEvent;

public interface EventReader {
    public SidratExecutionEvent find(Long time);
    public SidratExecutionEvent findFirst();
    public SidratExecutionEvent findNext(SidratExecutionEvent lastEvent);
    public SidratExecutionEvent findPrev(SidratExecutionEvent lastEvent);
    public Map<String,Object> locals(Long time);
    public List<SidratEvent> executions(String loc);
}
