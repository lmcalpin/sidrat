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
    
    public static void variableChanged(Object val, String variableTag, int lineNumber) {
        TrackedVariable var = SidratDebugger.instance().getLocalVariablesTracker().lookup(variableTag);
        SidratDebugger.instance().getEventStore().store(SidratLocalVariableEvent.variableChanged(val, var));
        if (logger.isDebugEnabled())
            logger.debug("variableChanged " + variableTag + " set to " + val + " @ " + lineNumber);
    }
    
    public static void variableChanged(int val, String variableTag, int lineNumber) {
        TrackedVariable var = SidratDebugger.instance().getLocalVariablesTracker().lookup(variableTag);
        SidratDebugger.instance().getEventStore().store(SidratLocalVariableEvent.variableChanged(new Integer(val), var));
        if (logger.isDebugEnabled())
            logger.debug("variableChanged " + variableTag + " set to " + val + " @ " + lineNumber);
    }
    
    public static void fieldChanged(Object obj, Object val, String name, int lineNumber) {
        SidratDebugger.instance().getEventStore().store(SidratFieldChangedEvent.fieldChanged(obj, val, name));
        if (logger.isDebugEnabled())
            logger.debug("fieldChanged " + name + " for object " + Objects.getUniqueIdentifier(obj) + " set to " + val + " @ " + lineNumber);
    }
    
    public static void fieldChanged(Object obj, int val, String name, int lineNumber) {
        SidratDebugger.instance().getEventStore().store(SidratFieldChangedEvent.fieldChanged(obj, new Integer(val), name));
        if (logger.isDebugEnabled())
            logger.debug("fieldChanged " + name + " for object " + Objects.getUniqueIdentifier(obj) + " set to " + val + " @ " + lineNumber);
    }
    
    public static void pushFrame(Object obj, String className, String methodName) {
        StackFrame frame = new StackFrame(obj, className, methodName);
        Stack<StackFrame> stackFrames = STACK_FRAMES.get();
        if (stackFrames == null) {
            stackFrames = new Stack<StackFrame>();
            STACK_FRAMES.set(stackFrames);
        }
        stackFrames.push(frame);
    }
    
    public static void popFrame() {
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
