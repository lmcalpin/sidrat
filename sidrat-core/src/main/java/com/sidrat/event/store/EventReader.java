package com.sidrat.event.store;

import java.util.List;
import java.util.Map;

import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.tracking.CapturedFieldValue;
import com.sidrat.event.tracking.CapturedLocalVariableValue;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.util.Pair;

public interface EventReader {
    /**
     * @return a map of an object's fields and current values at a specified time for a specified object
     */
    public Map<String, CapturedFieldValue> eval(Long time, String objectID);

    /**
     * @return a list of SidratEvents where we execute the line of code described by 'loc'
     */
    public List<SidratExecutionEvent> executions(String className, String methodName, int lineNumber);

    /**
     * @return history of assignments to a specified field
     */
    public List<Pair<Long, TrackedObject>> fieldHistory(String fieldID);

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
     * @return a map of local variables in scope at the specified time, and their current values
     */
    public Map<String, CapturedLocalVariableValue> locals(Long time);

    /**
     * @return history of assignments to a specified local variable
     */
    public List<Pair<Long, TrackedObject>> localVariableHistory(String localVariableID);
}
