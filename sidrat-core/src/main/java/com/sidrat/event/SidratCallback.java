package com.sidrat.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.sidrat.SidratDebugger;
import com.sidrat.event.tracking.ExecutionLocation;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.event.tracking.TrackedVariable;
import com.sidrat.util.Logger;
import com.sidrat.util.Objects;

public class SidratCallback {
    private static final Logger logger = new Logger();
    
    public static ThreadLocal<Stack<ExecutionLocation>> STACK_FRAMES = new ThreadLocal<Stack<ExecutionLocation>>();
    public static ThreadLocal<Boolean> ENTERED = new ThreadLocal<Boolean>();
    
    static {
        ENTERED.set(Boolean.FALSE);
    }

    // we use this map to link SidratExecutionEvent and SidratMethodExitEvents back to
    // the SidratMethodEntryEvent that preceded them on the current stack frame.
    public static Map<ExecutionLocation, SidratMethodEntryEvent> FRAME_EVENT_MAP = new HashMap<ExecutionLocation, SidratMethodEntryEvent>();
    
    public static void enter(Object obj, String clazz, String method, Object[] args) {
        TrackedObject trackedObj = SidratDebugger.instance().getObjectTracker().found(obj);
        pushFrame(trackedObj, clazz, method);
        SidratMethodEntryEvent event = SidratMethodEntryEvent.entering(currentFrame());
        SidratDebugger.instance().getEventStore().store(event);
        FRAME_EVENT_MAP.put(currentFrame(), event);
        ENTERED.set(Boolean.TRUE);
    }
    
    public static void enter(String clazz, String method, Object[] args) {
        enter(null, clazz, method, args);
    }
    
    public static void enter(String clazz, String method) {
        enter(null, clazz, method, new Object[]{});
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
        TrackedObject trackedObj = SidratDebugger.instance().getObjectTracker().found(returns);
        SidratMethodEntryEvent methodEntry = FRAME_EVENT_MAP.get(currentFrame());
        SidratMethodExitEvent event = SidratMethodExitEvent.exiting(methodEntry, trackedObj);
        SidratDebugger.instance().getEventStore().store(event);
        FRAME_EVENT_MAP.remove(currentFrame());
        popFrame();
    }
    
    public static void exec(int lineNumber) {
        SidratMethodEntryEvent methodEntry = FRAME_EVENT_MAP.get(currentFrame());
        ExecutionLocation frame = methodEntry.getExecutionContext();
        Long time = methodEntry.getTime();
        if (ENTERED.get().booleanValue()) {
            ENTERED.set(Boolean.FALSE);
        } else {
            time = SidratDebugger.instance().getClock().next();
        }
        SidratExecutionEvent executionEvent = SidratExecutionEvent.exec(time, methodEntry, lineNumber);
        SidratDebugger.instance().getEventStore().store(executionEvent);
        if (logger.isDebugEnabled())
            logger.debug(frame.getClassName() + "." + frame.getMethodName() + "@" + lineNumber);
    }
    
    public static void variableChanged(Object val, String var) {
        ExecutionLocation frame = currentFrame();
        TrackedVariable trackedVar = SidratDebugger.instance().getLocalVariablesTracker().lookup(frame.getClassName(), frame.getMethodName(), var);
        TrackedObject trackedObj = SidratDebugger.instance().getObjectTracker().found(val);
        SidratDebugger.instance().getEventStore().store(SidratLocalVariableEvent.variableChanged(trackedObj,
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
        TrackedObject trackedObj = SidratDebugger.instance().getObjectTracker().found(obj);
        TrackedObject trackedVal = SidratDebugger.instance().getObjectTracker().found(val);
        SidratDebugger.instance().getEventStore().store(SidratFieldChangedEvent.fieldChanged(trackedObj, trackedVal, name));
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
        Stack<ExecutionLocation> stackFrames = STACK_FRAMES.get();
        if (stackFrames == null) {
            stackFrames = new Stack<ExecutionLocation>();
            STACK_FRAMES.set(stackFrames);
        }
        stackFrames.push(frame);
    }
    
    static void popFrame() {
        Stack<ExecutionLocation> stackFrames = STACK_FRAMES.get();
        stackFrames.pop();
    }
    
    public static ExecutionLocation currentFrame() {
        Stack<ExecutionLocation> stack = STACK_FRAMES.get();
        if (stack == null)
            return null;
        if (stack.isEmpty())
            return null;
        return stack.peek();
    }
}
