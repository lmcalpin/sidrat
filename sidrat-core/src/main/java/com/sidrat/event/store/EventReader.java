package com.sidrat.event.store;

import java.util.List;
import java.util.Map;

import com.sidrat.event.SidratExecutionEvent;

public interface EventReader {
    /**
     * @return details about the SidratExecutionEvent at the specified time
     */
    public SidratExecutionEvent find(Long time);

    /**
     * @return details about the first recorded SidratExecutionEvent
     */
    public SidratExecutionEvent findFirst();

    /**
     * @return details about the SidratExecutionEvent that follows the supplied event
     */
    public SidratExecutionEvent findNext(SidratExecutionEvent lastEvent);

    /**
     * @return details about the SidratExecutionEvent that precedes the supplied event
     */
    public SidratExecutionEvent findPrev(SidratExecutionEvent lastEvent);

    /**
     * @return a list of local variables in scope at the specified time
     */
    public Map<String,Object> locals(Long time);
    
    /**
     * @return a list of SidratEvents where we execute the line of code described by 'loc'
     */
    public List<SidratExecutionEvent> executions(String className, String methodName, int lineNumber);
}
