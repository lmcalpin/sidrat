package com.sidrat.instrumentation;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sidrat.SidratProcessingException;
import com.sidrat.bytecode.Instruction;
import com.sidrat.bytecode.OperandStack;
import com.sidrat.bytecode.OperandStackValue;
import com.sidrat.bytecode.OperandValueType;
import com.sidrat.bytecode.ParameterLength;
import com.sidrat.event.tracking.LocalVariable;
import com.sidrat.util.Logger;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;

public class MethodInstrumenter {
    private static final Logger logger = new Logger();
    private final CtBehavior ctBehavior;
    private MethodInfo methodInfo;
    private CtClass cc;
    private final String className, methodName;

    private CodeAttribute ca;

    public MethodInstrumenter(CtBehavior ctBehavior) {
        this.ctBehavior = ctBehavior;
        this.cc = ctBehavior.getDeclaringClass();
        this.methodInfo = ctBehavior.getMethodInfo();
        this.className = cc.getName();
        this.methodName = ctBehavior.getMethodInfo().getName();
        this.ca = methodInfo.getCodeAttribute();
        if (ca == null)
            throw new ClassInstrumentationException("Error instrumenting " + className + "; no line number info");
    }

    public static Map<Integer, LocalVariable> getLocalVariables(CtBehavior ctBehavior) throws NotFoundException {
        Map<Integer, LocalVariable> variables = new HashMap<Integer, LocalVariable>();
        CodeAttribute codeAttribute = ctBehavior.getMethodInfo().getCodeAttribute();
        LocalVariableAttribute lva = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        if (lva != null) {
            for (int i = 0; i < lva.tableLength(); i++) {
                int rangeStart = lva.startPc(i);
                int rangeEnd = rangeStart + lva.codeLength(i);
                int lineNumberStart = ctBehavior.getMethodInfo().getLineNumber(rangeStart) - 1;
                int lineNumberEnd = lastLineNumber(ctBehavior, rangeStart, rangeEnd, lineNumberStart);
                LocalVariable localVariable = new LocalVariable(lva.variableName(i), lineNumberStart, lineNumberEnd);
                variables.put(i, localVariable);
            }
        }
        ;
        return variables;
    }

    private static int lastLineNumber(CtBehavior ctBehavior, int rangeStart, int rangeEnd, int lineNumberStart) {
        int lineNumberEnd = ctBehavior.getMethodInfo().getLineNumber(rangeEnd);
        if (rangeStart == rangeEnd) {
            return lineNumberStart;
        }
        // when inside a loop, the last bytecode may correspond to a line number *before* the point where the
        // variable was declared; in this case, we recursively attempt to determine the last line number *after* the
        // variable was declared where the variable is still in scope
        if (lineNumberEnd < lineNumberStart) {
            return lastLineNumber(ctBehavior, rangeStart, rangeEnd - 1, lineNumberStart);
        }
        return lineNumberEnd;
    }

    private String getLocalVariablesFromMethodSignature(Map<Integer, LocalVariable> variables) throws NotFoundException {
        int memberShift = Modifier.isStatic(ctBehavior.getModifiers()) ? 0 : 1;
        CtClass[] signatureTypes = ctBehavior.getParameterTypes();
        if (signatureTypes.length == 0)
            return "";
        StringBuilder signatureNames = new StringBuilder("");
        boolean foundVariable = false;
        for (int i = memberShift; i < signatureTypes.length + memberShift; i++) {
            if (foundVariable) {
                signatureNames.append(",");
            }
            LocalVariable lv = variables.get(i);
            if (lv != null) {
                signatureNames.append(lv.getName());
                foundVariable = true;
            }
        }
        return signatureNames.toString();
    }

    private int getLocalVariableSlot(int pos, int op) {
        CodeIterator ci = ca.iterator();
        ci.move(pos);
        String mnemonic = Mnemonic.OPCODE[op];
        String[] opCodeSlotRef = mnemonic.toUpperCase().split("_");
        int slot = opCodeSlotRef.length == 2 ? Integer.parseInt(opCodeSlotRef[1]) : ci.byteAt(pos + 1);
        return slot;
    }

    public void instrument() {
        int currentLineNumber = -1;
        try {
            LineNumberAttribute ainfo = (LineNumberAttribute) ca.getAttribute(LineNumberAttribute.tag);
            int flags = ctBehavior.getMethodInfo().getAccessFlags();
            Map<Integer, LocalVariable> variables = getLocalVariables(ctBehavior);
            Collection<Instruction> instructions = Instruction.analyze(ctBehavior, ca);
            OperandStack stack = new OperandStack(ca);
            for (Instruction instruction : instructions) {
                int op = instruction.getOpcode();
                int thisLineNumber = instruction.getLineNumber();
                if (op == Opcode.RET || op == Opcode.RETURN)
                    continue;
                if (thisLineNumber != currentLineNumber) {
                    currentLineNumber = thisLineNumber;
                    String src = "com.sidrat.event.SidratCallback.exec(" + currentLineNumber + ");";
                    instruction.insert(instructions, src);
                }
                logger.finest(instruction.toString());
                // track assignments to fields
                if (instruction.isFieldStore()) {
                    int methodRefIdx = instruction.getParameter(ParameterLength.U2);
                    String fieldName = ctBehavior.getMethodInfo().getConstPool().getFieldrefName(methodRefIdx);
                    String refClassName = ca.getConstPool().getFieldrefClassName(methodRefIdx);
                    boolean staticField = op == Opcode.PUTSTATIC;
                    if (staticField) {
                        String staticFieldRef = refClassName.replaceAll("\\$", ".") + "." + fieldName;
                        String src = "com.sidrat.event.SidratCallback.fieldChanged(" + refClassName + ".class, " + staticFieldRef + ",\"" + fieldName + "\");";
                        instruction.insert(instructions, src);
                    } else {
                        OperandStackValue stackValue = stack.peek2(); // it will be popped below when we run the stack simulation
                        if (stackValue == null || stackValue.getType() != OperandValueType.OBJECTREF) {
                            logger.severe("Stack is not in the expected state; we have a PUTFIELD without an OBJECTREF in the stack");
                        } else {
                            int prevOp = stackValue.getInstruction().getOpcode();
                            int slot = getLocalVariableSlot(stackValue.getInstruction().getPosition(), prevOp);
                            LocalVariable localVariable = variables.get(slot);
                            if (localVariable.getName().equals("this") && methodName.equals("<init>")) {
                                // TODO: track this after call to super completes
                                logger.severe("Failed to locate variable loaded using op [" + instruction + "] at line number " + thisLineNumber);
                            } else {
                                String src = "com.sidrat.event.SidratCallback.fieldChanged(" + localVariable.getName() + ", " + localVariable.getName() + "." + fieldName + ",\"" + fieldName + "\");";
                                instruction.insert(instructions, src);
                            }
                        }
                    }
                } else if (instruction.isLocalVariableUpdate()) {
                    // track assignments to local variables
                    if (instruction.isLocalVariableArrayUpdate()) {
                        OperandStackValue stackValue = stack.peek3();
                        int prevOp = stackValue.getInstruction().getOpcode();
                        int slot = getLocalVariableSlot(stackValue.getInstruction().getPosition(), prevOp);
                        LocalVariable localVariable = variables.get(slot);
                        if (localVariable != null) {
                            String src = "com.sidrat.event.SidratCallback.variableChanged(\"" + className + "\",\"" + methodName + "\"," + localVariable.getName() + ",\"" + localVariable.getName() + "\"," + localVariable.getStart() + ", "
                                    + localVariable.getEnd() + ");";
                            instruction.insert(instructions, src);
                        } else {
                            // TODO: effectively final parameters passed in to a lambda are not being handled properly and end up here
                            logger.severe("Failed to locate variable loaded using op [" + instruction + "] at line number " + thisLineNumber);
                        }
                    } else {
                        int slot = getLocalVariableSlot(instruction.getPosition(), op);
                        LocalVariable localVariable = variables.get(slot);
                        if (localVariable != null) {
                            String src = "com.sidrat.event.SidratCallback.variableChanged(\"" + className + "\",\"" + methodName + "\"," + localVariable.getName() + ",\"" + localVariable.getName() + "\"," + localVariable.getStart() + ", "
                                    + localVariable.getEnd() + ");";
                            instruction.insert(instructions, src);
                        } else {
                            // TODO: effectively final parameters passed in to a lambda are not being handled properly and end up here
                            logger.severe("Failed to locate variable loaded using op [" + instruction + "] at line number " + thisLineNumber);
                        }
                    }
                }
                stack.simulate(instruction);
            }
            int firstLineNumber = ainfo.lineNumber(0);
            int lastLineNumber = ainfo.lineNumber(ainfo.tableLength() - 1);
            logger.finer("Instrumented method " + methodName + " " + firstLineNumber + ":" + lastLineNumber);
            String signatureNames = getLocalVariablesFromMethodSignature(variables);
            if ((flags & AccessFlag.STATIC) != 0 || ctBehavior.getMethodInfo().isConstructor()) {
                ctBehavior.insertBefore("com.sidrat.event.SidratCallback.enter(Thread.currentThread().getName(), \"" + className + "\",\"" + methodName + "\", \"" + signatureNames + "\", $args);");
            } else {
                ctBehavior.insertBefore("com.sidrat.event.SidratCallback.enter($0, Thread.currentThread().getName(), \"" + className + "\",\"" + methodName + "\", \"" + signatureNames + "\", $args);");
            }
            ctBehavior.insertAfter("com.sidrat.event.SidratCallback.exit($_);");
        } catch (Exception e) {
            throw new SidratProcessingException(String.format("Error instrumenting: %s around line number %d", ctBehavior.getLongName(), currentLineNumber), e);
        }
    }
}
