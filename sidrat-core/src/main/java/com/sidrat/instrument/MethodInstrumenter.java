package com.sidrat.instrument;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.sidrat.SidratProcessingException;
import com.sidrat.event.tracking.LocalVariable;
import com.sidrat.util.Logger;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;

public class MethodInstrumenter implements Opcode {
    private final CtBehavior ctBehavior;
    private MethodInfo methodInfo;
    private CtClass cc;
    private final String className, methodName;
    private CodeAttribute ca;
    
    private static final Logger logger = new Logger();

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
    
    public void instrument() {
        try {
            LineNumberAttribute ainfo = (LineNumberAttribute) ca.getAttribute(LineNumberAttribute.tag);
            int flags = ctBehavior.getMethodInfo().getAccessFlags();
            Map<Integer, LocalVariable> variables = getLocalVariables(ctBehavior);
            Stack<Instruction> instructions = analyze(ca.iterator());
            Stack<OperandStackValue> operandStack = new Stack<>();
            int currentLineNumber = -1;
            for (Instruction instruction : instructions) {
                int op = instruction.getOpcode();
                int thisLineNumber = instruction.getLineNumber();
                if (op == RET || op == RETURN)
                    continue;
                if (thisLineNumber != currentLineNumber) {
                    currentLineNumber = thisLineNumber;
                    String src = "com.sidrat.event.SidratCallback.exec(" + currentLineNumber + ");";
                    instruction.insert(instructions, src);
                }
                logger.finest(instruction.toString());
                // track assignments to fields
                if (instruction.isFieldStore()) {
                    int methodRefIdx = instruction.getParameter();
                    String fieldName = ctBehavior.getMethodInfo().getConstPool().getFieldrefName(methodRefIdx);
                    String refClassName = ca.getConstPool().getFieldrefClassName(methodRefIdx);
                    boolean staticField = op == PUTSTATIC;
                    if (staticField) {
                        String staticFieldRef = refClassName.replaceAll("\\$", ".") + "." + fieldName;
                        String src = "com.sidrat.event.SidratCallback.fieldChanged(" + refClassName + ".class, " + staticFieldRef + ",\"" + fieldName + "\");";
                        instruction.insert(instructions, src);
                    } else {
                        int prevPos = instruction.prev(2).getPosition();
                        int prevOp = instruction.prev(2).getOpcode();
                        int slot = getLocalVariableSlot(prevPos, prevOp);
                        LocalVariable localVariable = variables.get(slot);
                        if (localVariable.getName().equals("this") && methodName.equals("<init>")) {
                            // TODO: track this after call to super completes
                            logger.severe("Failed to locate variable loaded using op [" + instruction + "] at line number " + thisLineNumber);
                        } else {
                            String src = "com.sidrat.event.SidratCallback.fieldChanged(" + localVariable.getName() + ", " + localVariable.getName() + "." + fieldName + ",\"" + fieldName + "\");";
                            instruction.insert(instructions, src);
                        }
                    }
                } else if (instruction.isLocalVariableUpdate()) {
                    // track assignments to local variables
                    if (instruction.isLocalVariableArrayUpdate()) {
                        logger.severe("Not supported (yet)");
                    } else {
                        int slot = getLocalVariableSlot(instruction.getPosition(), op);
                        LocalVariable localVariable = variables.get(slot);
                        if (localVariable != null) {
                            String src = "com.sidrat.event.SidratCallback.variableChanged(\"" + className + "\",\"" + methodName + "\"," + localVariable.getName() + ",\"" + localVariable.getName() + "\"," + localVariable.getStart() + ", " + localVariable.getEnd() + ");";
                            instruction.insert(instructions, src);
                        } else {
                            // TODO: effectively final parameters passed in to a lambda are not being handled properly and end up here
                            logger.severe("Failed to locate variable loaded using op [" + instruction + "] at line number " + thisLineNumber);
                        }
                    }
                }
            }
            int firstLineNumber = ainfo.lineNumber(0);
            int lastLineNumber = ainfo.lineNumber(ainfo.tableLength()-1);
            logger.finer("Instrumented method " + methodName + " " + firstLineNumber + ":" + lastLineNumber);
            //String signatureNames = getLocalVariablesFromMethodSignature(variables);
            if ((flags & AccessFlag.STATIC)  != 0 || ctBehavior.getMethodInfo().isConstructor()) {
//                ctBehavior.insertBefore("com.sidrat.event.SidratCallback.enter(\"" + className + "\",\""
//                        + methodName + "\", " + signatureNames + ", $args);");
              ctBehavior.insertBefore("com.sidrat.event.SidratCallback.enter(Thread.currentThread().getName(), \"" + className + "\",\""
              + methodName + "\");");
            } else {
//                ctBehavior.insertBefore("com.sidrat.event.SidratCallback.enter($0, \"" + className + "\",\""
//                        + methodName + "\", " + signatureNames + ", $args);");
                ctBehavior.insertBefore("com.sidrat.event.SidratCallback.enter($0, Thread.currentThread().getName(), \"" + className + "\",\""
                        + methodName + "\");");
            }
            ctBehavior.insertAfter("com.sidrat.event.SidratCallback.exit($_);");
        } catch (CannotCompileException e) {
            throw new SidratProcessingException("Error instrumenting: " + ctBehavior.getLongName(), e);
        } catch (BadBytecode e) {
            throw new SidratProcessingException("Error instrumenting: " + ctBehavior.getLongName(), e);
        } catch (NotFoundException e) {
            throw new SidratProcessingException("Error instrumenting: " + ctBehavior.getLongName(), e);
        } catch (Throwable t) {
            throw new SidratProcessingException("Error instrumenting: " + ctBehavior, t);
        }
    }
    
    private Stack<Instruction> analyze(CodeIterator ci) throws BadBytecode {
        Stack<Instruction> instructions = new Stack<Instruction>();
        Instruction last = null;
        while (ci.hasNext()) {
            int pos = ci.next();
            int op = ci.byteAt(pos);
            int next = ci.lookAhead();
            int thisLineNumber = methodInfo.getLineNumber(pos);
            Instruction instruction = new Instruction(pos, thisLineNumber, op, next, last, ci, ctBehavior);
            last = instruction;
            instructions.push(instruction);
        }
        return instructions;
    }

    private int getLocalVariableSlot(int pos, int op) {
        CodeIterator ci = ca.iterator();
        ci.move(pos);
        String mnemonic = Mnemonic.OPCODE[op];
        String[] opCodeSlotRef = mnemonic.toUpperCase().split("_");
        int slot = opCodeSlotRef.length == 2 ? Integer.parseInt(opCodeSlotRef[1]) : ci.byteAt(pos + 1);
        return slot;
    }
    
    public static Map<Integer, LocalVariable> getLocalVariables(CtBehavior ctBehavior) throws NotFoundException {
        Map<Integer, LocalVariable> variables = new HashMap<Integer, LocalVariable>();
        CodeAttribute codeAttribute = ctBehavior.getMethodInfo().getCodeAttribute();
        LocalVariableAttribute lva = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        if (lva != null) {
            for(int i = 0; i < lva.tableLength(); i++) {
                int rangeStart = lva.startPc(i);
                int rangeEnd = rangeStart + lva.codeLength(i);
                int lineNumberStart = ctBehavior.getMethodInfo().getLineNumber(rangeStart) - 1;
                int lineNumberEnd = lastLineNumber(ctBehavior, rangeStart, rangeEnd, lineNumberStart);
                LocalVariable localVariable = new LocalVariable(lva.variableName(i), lineNumberStart, lineNumberEnd);
                variables.put(i, localVariable);
            }
        };
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
}
