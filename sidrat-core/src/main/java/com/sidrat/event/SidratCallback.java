package com.sidrat.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.sidrat.SidratRegistry;
import com.sidrat.event.tracking.ExecutionLocation;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.event.tracking.TrackedVariable;
import com.sidrat.util.Logger;
import com.sidrat.util.Objects;
import com.sidrat.util.Pair;
import com.sidrat.util.ZipUtils;

public class SidratCallback {
    private static final Logger logger = new Logger();
    
    public static ThreadLocal<Pair<ExecutionLocation,Integer>> LAST_LINE = new ThreadLocal<Pair<ExecutionLocation,Integer>>();
    public static ThreadLocal<Stack<ExecutionLocation>> CALL_STACK = new ThreadLocal<Stack<ExecutionLocation>>();
    public static ThreadLocal<Boolean> ENTERED = new ThreadLocal<Boolean>();
    
    static {
        ENTERED.set(Boolean.FALSE);
    }

    // we use this map to link SidratExecutionEvent and SidratMethodExitEvents back to
    // the SidratMethodEntryEvent that preceded them on the current stack frame.
    public static Map<ExecutionLocation, SidratMethodEntryEvent> FRAME_EVENT_MAP = new HashMap<ExecutionLocation, SidratMethodEntryEvent>();
    
    public static void enter(Object obj, String clazz, String method, String[] names, Object[] args) {
        Map<String,Object> argMap = Collections.emptyMap();
        if (names != null) {
            argMap = ZipUtils.zipAsMap(names, args);
        }
        TrackedObject trackedObj = SidratRegistry.instance().getRecorder().getObjectTracker().found(obj);
        pushFrame(trackedObj, clazz, method);
        SidratMethodEntryEvent event = SidratMethodEntryEvent.entering(currentFrame(), argMap);
        SidratRegistry.instance().getRecorder().getEventStore().store(event);
        FRAME_EVENT_MAP.put(currentFrame(), event);
        ENTERED.set(Boolean.TRUE);
    }
    
    public static void enter(String clazz, String method, String[] names, Object[] args) {
        enter(null, clazz, method, names, args);
    }
    
    /**
     * Only used by tests as a convenience... TODO: should be eliminated
     * @deprecated
     */
    @Deprecated
    public static void enter(String clazz, String method) {
        enter(null, clazz, method, new String[]{}, new Object[]{});
    }
    
    public static void exit(byte val) {
        exit(Byte.valueOf(val));
    }
    
    public static void exit(int val) {
        exit(Integer.valueOf(val));
    }
    
    public static void exit(short val) {
        exit(Short.valueOf(val));
    }
    
    public static void exit(long val) {
        exit(Long.valueOf(val));
    }
    
    public static void exit(boolean val) {
        exit(Boolean.valueOf(val));
    }
    
    public static void exit(float val) {
        exit(Float.valueOf(val));
    }
    
    public static void exit(double val) {
        exit(Double.valueOf(val));
    }
    
    public static void exit(char val) {
        exit(Character.valueOf(val));
    }
    
    public static void exit(Object returns) {
        TrackedObject trackedObj = SidratRegistry.instance().getRecorder().getObjectTracker().found(returns);
        SidratMethodEntryEvent methodEntry = FRAME_EVENT_MAP.get(currentFrame());
        SidratMethodExitEvent event = SidratMethodExitEvent.exiting(methodEntry, trackedObj);
        SidratRegistry.instance().getRecorder().getEventStore().store(event);
        FRAME_EVENT_MAP.remove(currentFrame());
        popFrame();
        LAST_LINE.set(null);
    }
    
    public static void exec(int lineNumber) {
        // a single line of code might be invoked if there are multiple statements on the same line (or a for statement), but
        // we only want to log each line of code once
        Pair<ExecutionLocation,Integer> lastLoc = LAST_LINE.get();
        if (lastLoc != null && lastLoc.getValue1().equals(currentFrame()) && lastLoc.getValue2() == lineNumber) {
            // don't do anything if we already processed this line
            return;
        }
        
        // log this event to the event store
        SidratMethodEntryEvent methodEntry = FRAME_EVENT_MAP.get(currentFrame());
        LAST_LINE.set(new Pair<ExecutionLocation,Integer>(currentFrame(), lineNumber));
        ExecutionLocation frame = methodEntry.getExecutionContext();
        Long time = methodEntry.getTime();
        if (ENTERED.get().booleanValue()) {
            ENTERED.set(Boolean.FALSE);
        } else {
            time = SidratRegistry.instance().getRecorder().getClock().next();
        }
        SidratExecutionEvent executionEvent = SidratExecutionEvent.exec(time, methodEntry, lineNumber);
        SidratRegistry.instance().getRecorder().getEventStore().store(executionEvent);
        if (logger.isDebugEnabled())
            logger.debug(frame.getClassName() + "." + frame.getMethodName() + "@" + lineNumber);
    }
    
    public static void variableChanged(Object val, String var) {
        ExecutionLocation frame = currentFrame();
        TrackedVariable trackedVar = SidratRegistry.instance().getRecorder().getLocalVariablesTracker().lookup(frame.getClassName(), frame.getMethodName(), var);
        TrackedObject trackedObj = SidratRegistry.instance().getRecorder().getObjectTracker().found(val);
        SidratRegistry.instance().getRecorder().getEventStore().store(SidratLocalVariableEvent.variableChanged(trackedObj,
                trackedVar));
        if (logger.isDebugEnabled())
            logger.debug("variableChanged " + var + " set to " + val);
    }
    
    public static void variableChanged(byte val, String var) {
        variableChanged(Byte.valueOf(val), var);
    }
    
    public static void variableChanged(int val, String var) {
        variableChanged(Integer.valueOf(val), var);
    }
    
    public static void variableChanged(short val, String var) {
        variableChanged(Short.valueOf(val), var);
    }
    
    public static void variableChanged(long val, String var) {
        variableChanged(Long.valueOf(val), var);
    }
    
    public static void variableChanged(boolean val, String var) {
        variableChanged(Boolean.valueOf(val), var);
    }
    
    public static void variableChanged(float val, String var) {
        variableChanged(Float.valueOf(val), var);
    }
    
    public static void variableChanged(double val, String var) {
        variableChanged(Double.valueOf(val), var);
    }
    
    public static void variableChanged(char val, String var) {
        variableChanged(Character.valueOf(val), var);
    }
    
    public static void fieldChanged(Object obj, Object val, String name) {
        TrackedObject trackedObj = SidratRegistry.instance().getRecorder().getObjectTracker().found(obj);
        TrackedObject trackedVal = SidratRegistry.instance().getRecorder().getObjectTracker().found(val);
        SidratRegistry.instance().getRecorder().getEventStore().store(SidratFieldChangedEvent.fieldChanged(trackedObj, trackedVal, name));
        if (logger.isDebugEnabled())
            logger.debug("fieldChanged " + name + " for object " + Objects.getUniqueIdentifier(obj) + " set to " + val);
    }
    
    public static void fieldChanged(Object obj, byte val, String name) {
        fieldChanged(obj, Byte.valueOf(val), name);
    }
    
    public static void fieldChanged(Object obj, int val, String name) {
        fieldChanged(obj, Integer.valueOf(val), name);
    }
    
    public static void fieldChanged(Object obj, short val, String name) {
        fieldChanged(obj, Short.valueOf(val), name);
    }
    
    public static void fieldChanged(Object obj, long val, String name) {
        fieldChanged(obj, Long.valueOf(val), name);
    }
    
    public static void fieldChanged(Object obj, boolean val, String name) {
        fieldChanged(obj, Boolean.valueOf(val), name);
    }
    
    public static void fieldChanged(Object obj, float val, String name) {
        fieldChanged(obj, Float.valueOf(val), name);
    }
    
    public static void fieldChanged(Object obj, double val, String name) {
        fieldChanged(obj, Double.valueOf(val), name);
    }
    
    public static void fieldChanged(Object obj, char val, String name) {
        fieldChanged(obj, Character.valueOf(val), name);
    }
    
    static void pushFrame(TrackedObject object, String className, String methodName) {
        ExecutionLocation frame = new ExecutionLocation(object, className, methodName);
        Stack<ExecutionLocation> stackFrames = CALL_STACK.get();
        if (stackFrames == null) {
            stackFrames = new Stack<ExecutionLocation>();
            CALL_STACK.set(stackFrames);
        }
        stackFrames.push(frame);
    }
    
    static void popFrame() {
        Stack<ExecutionLocation> stackFrames = CALL_STACK.get();
        stackFrames.pop();
    }
    
    public static ExecutionLocation currentFrame() {
        Stack<ExecutionLocation> stack = CALL_STACK.get();
        if (stack == null)
            return null;
        if (stack.isEmpty())
            return null;
        return stack.peek();
    }
}
