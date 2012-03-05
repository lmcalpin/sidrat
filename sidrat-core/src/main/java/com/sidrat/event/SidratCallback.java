package com.sidrat.event;

import java.util.Stack;

import com.sidrat.SidratDebugger;
import com.sidrat.event.tracking.StackFrame;
import com.sidrat.event.tracking.TrackedVariable;
import com.sidrat.util.Logger;
import com.sidrat.util.Objects;

public class SidratCallback {
    private static final Logger logger = new Logger();
    
    public static ThreadLocal<Stack<StackFrame>> STACK_FRAMES = new ThreadLocal<Stack<StackFrame>>();

    // yuck... we use this threadlocal to let us know which call to exec is the first one for a specific 
    // method invocation
    public static ThreadLocal<Boolean> ENTERING_FRAME = new ThreadLocal<Boolean>();
    
    static {
        ENTERING_FRAME.set(Boolean.FALSE);
    }
    
    public static void enter(Object obj, String clazz, String method) {
        pushFrame(obj, clazz, method);
        ENTERING_FRAME.set(Boolean.TRUE);
    }
    
    public static void enter(String clazz, String method) {
        enter(null, clazz, method);
    }
    
    public static void exit() {
        popFrame();
    }
    
    public static void exec(int lineNumber) {
        StackFrame frame = currentFrame();
        Object executionContext = frame.getObject();
        SidratExecutionEvent executionEvent = SidratExecutionEvent.exec(executionContext, lineNumber, ENTERING_FRAME.get());
        ENTERING_FRAME.set(Boolean.FALSE);
        SidratDebugger.instance().getEventStore().store(executionEvent);
        if (logger.isDebugEnabled())
            logger.debug(frame.getClassName() + "." + frame.getMethodName() + "@" + lineNumber);
    }
    
    public static void variableChanged(Object val, String var) {
        StackFrame frame = currentFrame();
        TrackedVariable trackedVar = SidratDebugger.instance().getLocalVariablesTracker().lookup(frame.getClassName(), frame.getMethodName(), var);
        SidratDebugger.instance().getEventStore().store(SidratLocalVariableEvent.variableChanged(val, trackedVar));
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
        SidratDebugger.instance().getEventStore().store(SidratFieldChangedEvent.fieldChanged(obj, val, name));
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
    
    static void pushFrame(Object obj, String className, String methodName) {
        StackFrame frame = new StackFrame(obj, className, methodName);
        Stack<StackFrame> stackFrames = STACK_FRAMES.get();
        if (stackFrames == null) {
            stackFrames = new Stack<StackFrame>();
            STACK_FRAMES.set(stackFrames);
        }
        stackFrames.push(frame);
    }
    
    static void popFrame() {
        Stack<StackFrame> stackFrames = STACK_FRAMES.get();
        stackFrames.pop();
    }
    
    public static StackFrame currentFrame() {
        Stack<StackFrame> stack = STACK_FRAMES.get();
        if (stack == null)
            return null;
        if (stack.isEmpty())
            return null;
        return stack.peek();
    }
}
