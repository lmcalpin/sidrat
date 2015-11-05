package com.sidrat.event;

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

    private static boolean isRecording = false;

    public static ThreadLocal<Pair<ExecutionLocation, Integer>> LAST_LINE = new ThreadLocal<Pair<ExecutionLocation, Integer>>();
    public static ThreadLocal<Stack<Pair<ExecutionLocation, SidratMethodEntryEvent>>> CALL_STACK = new ThreadLocal<>();
    public static ThreadLocal<Boolean> ENTERED = new ThreadLocal<Boolean>();
    public static ThreadLocal<Boolean> EXECUTING = new ThreadLocal<Boolean>();

    public static Pair<ExecutionLocation, SidratMethodEntryEvent> currentFrame() {
        Stack<Pair<ExecutionLocation, SidratMethodEntryEvent>> stack = CALL_STACK.get();
        if (stack == null)
            return null;
        if (stack.isEmpty())
            return null;
        return stack.peek();
    }

    public static void enter(Object obj, String threadName, String clazz, String method, String argNames, Object[] argValues) {
        if (!isRecording)
            return;
        preventRecursion(() -> {
            TrackedObject trackedObj = SidratRegistry.instance().getRecorder().getObjectTracker().found(obj);
            ExecutionLocation executionLocation = getExecutionLocation(trackedObj, threadName, clazz, method);
            if (argValues != null && argValues.length > 0) {
                String[] args = argNames.split(",");
                Map<String, Object> argMap = ZipUtils.zipAsMap(args, argValues, false);
                for (String var : argMap.keySet()) {
                    Object val = argMap.get(var);
                    SidratRegistry.instance().getRecorder().getLocalVariablesTracker().lookup(executionLocation.getClassName(), executionLocation.getMethodName(), var);
                    SidratRegistry.instance().getRecorder().getObjectTracker().found(val);
                }
            }
            SidratMethodEntryEvent event = SidratMethodEntryEvent.entering(executionLocation);
            SidratRegistry.instance().getRecorder().getEventStore().store(event);
            CALL_STACK.get().add(new Pair<>(executionLocation, event));
            ENTERED.set(Boolean.TRUE);
        });
    }

    public static void enter(String threadName, String clazz, String method, String argNames, Object[] argValues) {
        enter(null, threadName, clazz, method, argNames, argValues);
    }

    public static void exec(int lineNumber) {
        if (!isRecording)
            return;
        // a single line of code might be invoked if there are multiple statements on the same line (or a for statement), but
        // we only want to log each line of code once
        Pair<ExecutionLocation, Integer> lastLoc = LAST_LINE.get();
        if (lastLoc != null && lastLoc.getValue1().equals(currentFrame().getValue1()) && lastLoc.getValue2() == lineNumber) {
            // don't do anything if we already processed this line
            return;
        }

        preventRecursion(() -> {
            // log this event to the event store
            ExecutionLocation location = peekFrame().getValue1();
            SidratMethodEntryEvent methodEntry = peekFrame().getValue2();
            LAST_LINE.set(new Pair<ExecutionLocation, Integer>(location, lineNumber));
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
        });
    }

    public static void exit(boolean val) {
        exit(Boolean.valueOf(val));
    }

    public static void exit(byte val) {
        exit(Byte.valueOf(val));
    }

    public static void exit(char val) {
        exit(Character.valueOf(val));
    }

    public static void exit(double val) {
        exit(Double.valueOf(val));
    }

    public static void exit(float val) {
        exit(Float.valueOf(val));
    }

    public static void exit(int val) {
        exit(Integer.valueOf(val));
    }

    public static void exit(long val) {
        exit(Long.valueOf(val));
    }

    public static void exit(Object returns) {
        if (!isRecording)
            return;
        preventRecursion(() -> {
            TrackedObject trackedObj = SidratRegistry.instance().getRecorder().getObjectTracker().found(returns);
            SidratMethodEntryEvent methodEntry = peekFrame().getValue2();
            SidratMethodExitEvent event = SidratMethodExitEvent.exiting(methodEntry, trackedObj);
            SidratRegistry.instance().getRecorder().getEventStore().store(event);
            popFrame();
            LAST_LINE.set(null);
            EXECUTING.set(Boolean.FALSE);
        });
    }

    public static void exit(short val) {
        exit(Short.valueOf(val));
    }

    public static void fieldChanged(Object obj, boolean val, String name) {
        fieldChanged(obj, Boolean.valueOf(val), name);
    }

    public static void fieldChanged(Object obj, byte val, String name) {
        fieldChanged(obj, Byte.valueOf(val), name);
    }

    public static void fieldChanged(Object obj, char val, String name) {
        fieldChanged(obj, Character.valueOf(val), name);
    }

    public static void fieldChanged(Object obj, double val, String name) {
        fieldChanged(obj, Double.valueOf(val), name);
    }

    public static void fieldChanged(Object obj, float val, String name) {
        fieldChanged(obj, Float.valueOf(val), name);
    }

    public static void fieldChanged(Object obj, int val, String name) {
        fieldChanged(obj, Integer.valueOf(val), name);
    }

    public static void fieldChanged(Object obj, long val, String name) {
        fieldChanged(obj, Long.valueOf(val), name);
    }

    public static void fieldChanged(Object obj, Object val, String name) {
        if (!isRecording)
            return;
        preventRecursion(() -> {
            TrackedObject trackedObj = SidratRegistry.instance().getRecorder().getObjectTracker().found(obj);
            TrackedObject trackedVal = SidratRegistry.instance().getRecorder().getObjectTracker().found(val);
            SidratRegistry.instance().getRecorder().getEventStore().store(SidratFieldChangedEvent.fieldChanged(trackedObj, trackedVal, name));

            // go down the stack and find out if we have any local variables pointing to this object
            // track the value change so when they come back in scope, we will know the current value
            // TODO: this should be handled by the store so we can normalize the tracked object values
            Stack<Pair<ExecutionLocation, SidratMethodEntryEvent>> stack = CALL_STACK.get();
            for (Pair<ExecutionLocation, SidratMethodEntryEvent> location : stack) {
                Map<TrackedVariable, TrackedObject> variables = location.getValue1().getEncounteredVariables();
                for (TrackedVariable var : variables.keySet()) {
                    TrackedObject referencingObj = variables.get(var);
                    if (referencingObj.getUniqueID().equals(trackedObj.getUniqueID())) {
                        SidratRegistry.instance().getRecorder().getEventStore().store(SidratLocalVariableEvent.variableChanged(trackedObj, var));
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("fieldChanged " + name + " for object " + Objects.getUniqueIdentifier(obj) + " set to " + val);
            }
        });
    }

    public static void fieldChanged(Object obj, short val, String name) {
        fieldChanged(obj, Short.valueOf(val), name);
    }

    static ExecutionLocation getExecutionLocation(TrackedObject object, String threadName, String className, String methodName) {
        ExecutionLocation frame = new ExecutionLocation(object, threadName, className, methodName);
        return frame;
    }

    static Pair<ExecutionLocation, SidratMethodEntryEvent> peekFrame() {
        Stack<Pair<ExecutionLocation, SidratMethodEntryEvent>> stackFrames = CALL_STACK.get();
        return stackFrames.peek();
    }

    static void popFrame() {
        Stack<?> stackFrames = CALL_STACK.get();
        stackFrames.pop();
    }

    private static void preventRecursion(Runnable r) {
        if (EXECUTING.get() != null && EXECUTING.get().booleanValue())
            return;
        try {
            EXECUTING.set(Boolean.TRUE);
            r.run();
        } finally {
            EXECUTING.set(Boolean.FALSE);
        }
    }

    static void pushFrame(Pair<ExecutionLocation, SidratMethodEntryEvent> frame) {
        Stack<Pair<ExecutionLocation, SidratMethodEntryEvent>> stackFrames = CALL_STACK.get();
        if (stackFrames == null) {
            stackFrames = new Stack<>();
            CALL_STACK.set(stackFrames);
        }
        stackFrames.push(frame);
    }

    public static void startRecording() {
        ENTERED.set(Boolean.FALSE);
        EXECUTING.set(Boolean.FALSE);
        CALL_STACK.set(new Stack<>());
        isRecording = true;
    }

    public static void stopRecording() {
        isRecording = false;
    }

    public static void variableChanged(String className, String methodName, boolean val, String var, int start, int end) {
        variableChanged(className, methodName, Boolean.valueOf(val), var, start, end);
    }

    public static void variableChanged(String className, String methodName, byte val, String var, int start, int end) {
        variableChanged(className, methodName, Byte.valueOf(val), var, start, end);
    }

    public static void variableChanged(String className, String methodName, char val, String var, int start, int end) {
        variableChanged(className, methodName, Character.valueOf(val), var, start, end);
    }

    public static void variableChanged(String className, String methodName, double val, String var, int start, int end) {
        variableChanged(className, methodName, Double.valueOf(val), var, start, end);
    }

    public static void variableChanged(String className, String methodName, float val, String var, int start, int end) {
        variableChanged(className, methodName, Float.valueOf(val), var, start, end);
    }

    public static void variableChanged(String className, String methodName, int val, String var, int start, int end) {
        variableChanged(className, methodName, Integer.valueOf(val), var, start, end);
    }

    public static void variableChanged(String className, String methodName, long val, String var, int start, int end) {
        variableChanged(className, methodName, Long.valueOf(val), var, start, end);
    }

    public static void variableChanged(String className, String methodName, Object val, String var, int start, int end) {
        if (!isRecording)
            return;
        preventRecursion(() -> {
            SidratRegistry.instance().getRecorder().getLocalVariablesTracker().found(className, methodName, var, start, end);
            Pair<ExecutionLocation, SidratMethodEntryEvent> frame = currentFrame();
            ExecutionLocation executionLocation = frame.getValue1();
            TrackedVariable trackedVar = SidratRegistry.instance().getRecorder().getLocalVariablesTracker().lookup(executionLocation.getClassName(), executionLocation.getMethodName(), var);
            if (trackedVar == null) {
                logger.severe("Couldn't find " + var + " in " + executionLocation);
                return;
            }
            TrackedObject trackedObj = SidratRegistry.instance().getRecorder().getObjectTracker().found(val);
            executionLocation.track(trackedVar, trackedObj);
            SidratRegistry.instance().getRecorder().getEventStore().store(SidratLocalVariableEvent.variableChanged(trackedObj, trackedVar));
            if (logger.isDebugEnabled())
                logger.debug("variableChanged " + trackedVar + " set to " + val);
        });
    }

    public static void variableChanged(String className, String methodName, short val, String var, int start, int end) {
        variableChanged(className, methodName, Short.valueOf(val), var, start, end);
    }
}
