package com.sidrat.instrument;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.sidrat.SidratProcessingException;
import com.sidrat.SidratRegistry;
import com.sidrat.util.Logger;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;

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
    
    // TODO: clean this up
    public static class StackFrame {
        int pos;
        int original_pos;
        int lineNumber;
        int op;
        int next;
        int param;
    }

    public void instrument() {
        try {
            LineNumberAttribute ainfo = (LineNumberAttribute) ca.getAttribute(LineNumberAttribute.tag);
            int flags = ctBehavior.getMethodInfo().getAccessFlags();
            CodeIterator ci = ca.iterator();
            Map<Integer, LocalVariable> variables = getLocalVariables(ctBehavior);
            // TODO: temp hack...
            Stack<LocalVariable> variable = new Stack<>();
            Stack<StackFrame> stackFrames = analyze(ca.iterator());
            int currentLineNumber = -1;
            for (StackFrame sf : stackFrames) {
                int op = sf.op;
                String mnemonic = Mnemonic.OPCODE[op];
                int thisLineNumber = sf.lineNumber;
                if (op == RET || op == RETURN)
                    continue;
                if (thisLineNumber != currentLineNumber) {
                    currentLineNumber = thisLineNumber;
                    String src = "com.sidrat.event.SidratCallback.exec(" + currentLineNumber + ");";
                    compile(stackFrames, sf, src);
                }
                logger.finest(thisLineNumber + ": [pos#" + sf.pos + ", original#" + sf.original_pos + "] " + mnemonic);
                ci.move(sf.pos);
                // track assignments to fields
                if (isFieldStore(op)) {
                    int methodRefIdx = sf.param;
                    String fieldName = ctBehavior.getMethodInfo().getConstPool().getFieldrefName(methodRefIdx);
                    String refClassName = ca.getConstPool().getFieldrefClassName(methodRefIdx);
                    boolean staticField = op == PUTSTATIC;
                    if (staticField) {
                        String staticFieldRef = refClassName.replaceAll("\\$", ".") + "." + fieldName;
                        String src = "com.sidrat.event.SidratCallback.fieldChanged(" + refClassName + ".class, " + staticFieldRef + ",\"" + fieldName + "\");";
                        compile(stackFrames, sf, src);
                    } else {
                        LocalVariable localVariable = variable.pop();
                        if (!variable.isEmpty())
                            localVariable = variable.peek();
                        String src = "com.sidrat.event.SidratCallback.fieldChanged(" + localVariable.name + ", " + localVariable.name + "." + fieldName + ",\"" + fieldName + "\");";
                        compile(stackFrames, sf, src);
                    }
                    variable.clear();
                } else if (isLocalVariableLoad(op)) {
                    // track assignments to local variables
                    int slot = getLocalVariableSlot(ci, sf.original_pos, op);
                    LocalVariable localVariable = variables.get(slot);
                    logger.finest("push: " + localVariable);
                    variable.push(localVariable);
                } else if (isLocalVariableUpdate(op)) {
                    // track assignments to local variables
                    int slot = getLocalVariableSlot(ci, sf.original_pos, op);
                    LocalVariable localVariable = variables.get(slot);
                    if (localVariable != null) {
                        SidratRegistry.instance().getRecorder().getLocalVariablesTracker().found(ctBehavior, localVariable);
                        String src = "com.sidrat.event.SidratCallback.variableChanged(" + localVariable.name + ",\"" + localVariable.name + "\");";
                        compile(stackFrames, sf, src);
                    } else {
                        logger.severe("Failed to locate variable loaded using op [" + op + " / " + mnemonic + "] at line number " + thisLineNumber);
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
              ctBehavior.insertBefore("com.sidrat.event.SidratCallback.enter(\"" + className + "\",\""
              + methodName + "\");");
            } else {
//                ctBehavior.insertBefore("com.sidrat.event.SidratCallback.enter($0, \"" + className + "\",\""
//                        + methodName + "\", " + signatureNames + ", $args);");
                ctBehavior.insertBefore("com.sidrat.event.SidratCallback.enter($0, \"" + className + "\",\""
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
    
    private Stack<StackFrame> analyze(CodeIterator ci) throws BadBytecode {
        Stack<StackFrame> stackFrames = new Stack<StackFrame>();
        while (ci.hasNext()) {
            int pos = ci.next();
            int op = ci.byteAt(pos);
            StackFrame sf = new StackFrame();
            sf.pos = pos;
            sf.original_pos = pos;
            sf.op = op;
            sf.next = ci.lookAhead();
            if (isFieldStore(op))
                sf.param = ci.u16bitAt(pos + 1);
            int thisLineNumber = methodInfo.getLineNumber(pos);
            sf.lineNumber = thisLineNumber;
            logger.finest(thisLineNumber + ": " + pos + ": " + Mnemonic.OPCODE[op]);
            stackFrames.push(sf);
        }
        return stackFrames;
    }

    private int getLocalVariableSlot(CodeIterator ci, int pos, int op) {
        String mnemonic = Mnemonic.OPCODE[op];
        String[] opCodeSlotRef = mnemonic.toUpperCase().split("_");
        int slot = opCodeSlotRef.length == 2 ? Integer.parseInt(opCodeSlotRef[1]) : ci.byteAt(pos + 1);
        return slot;
    }
    
    private static boolean isFieldStore(int op) {
        return op == PUTSTATIC || op == PUTFIELD;
    }

    private static boolean isLocalVariableUpdate(int op) {
        if (op == ISTORE || op == LSTORE || op == FSTORE || op == DSTORE || op == ASTORE)
            return true;
        if (op == ISTORE_0 || op == ISTORE_1 || op == ISTORE_2 || op == ISTORE_3)
            return true;
        if (op == LSTORE_0 || op == LSTORE_1 || op == LSTORE_2 || op == LSTORE_3)
            return true;
        if (op == FSTORE_0 || op == FSTORE_1 || op == FSTORE_2 || op == FSTORE_3)
            return true;
        if (op == DSTORE_0 || op == DSTORE_1 || op == DSTORE_2 || op == DSTORE_3)
            return true;
        if (op == ASTORE_0 || op == ASTORE_1 || op == ASTORE_2 || op == ASTORE_3)
            return true;
        if (op == IINC)
            return true;
        return false;
    }
    
    private static boolean isLocalVariableLoad(int op) {
        if (op == ILOAD || op == LLOAD || op == FLOAD || op == DLOAD || op == ALOAD)
            return true;
        if (op == ILOAD_0 || op == ILOAD_1 || op == ILOAD_2 || op == ILOAD_3)
            return true;
        if (op == LLOAD_0 || op == LLOAD_1 || op == LLOAD_2 || op == LLOAD_3)
            return true;
        if (op == FLOAD_0 || op == FLOAD_1 || op == FLOAD_2 || op == FLOAD_3)
            return true;
        if (op == DLOAD_0 || op == DLOAD_1 || op == DLOAD_2 || op == DLOAD_3)
            return true;
        if (op == ALOAD_0 || op == ALOAD_1 || op == ALOAD_2 || op == ALOAD_3)
            return true;
        return false;
    }

    private void compile(Stack<StackFrame> stackFrames, StackFrame sf, String src) throws BadBytecode, CannotCompileException, NotFoundException {
        try {
            logger.finer("compiling \"" + src + "\" for " + sf.lineNumber);
            CodeIterator ci = ca.iterator();
            ci.move(sf.next);
            byte[] code = compileImpl(sf, src);
            ci.insert(code);
            int offset = code.length;
            for (StackFrame frame : stackFrames) {
                if (frame.pos > sf.pos) {
                    frame.pos += offset;
                    frame.next += offset;
                }
            }
        } catch (CannotCompileException e) {
            logger.severe("Failed to compile [" + src + "] at line number [" + sf.lineNumber + "]");
        }
    }

    private byte[] compileImpl(StackFrame sf, String src) throws CannotCompileException, BadBytecode, NotFoundException {
        Javac jv = new Javac(cc);
        try {
            jv.recordLocalVariables(ca, sf.next);
            jv.recordParams(ctBehavior.getParameterTypes(), Modifier.isStatic(ctBehavior.getModifiers()));
            jv.setMaxLocals(ca.getMaxLocals());
            jv.compileStmnt(src);
            Bytecode b = jv.getBytecode();
            int locals = b.getMaxLocals();
            ca.setMaxLocals(locals);
            int stack = b.getMaxStack();
            if (stack > ca.getMaxStack())
                ca.setMaxStack(stack);
            return b.get();
        } catch (CompileError e) {
            throw new CannotCompileException(e);
        }
    }

    /*
    private String getLocalVariablesFromMethodSignature(Map<Integer, LocalVariable> variables) throws NotFoundException {
        int memberShift = Modifier.isStatic(ctBehavior.getModifiers()) ? 0 : 1;
        CtClass[] signatureTypes = ctBehavior.getParameterTypes();
        if (signatureTypes.length == 0)
            return "null";
        StringBuilder signatureNames = new StringBuilder("new java.lang.String[] {");
        boolean foundVariable = false;
        for (int i = memberShift; i < signatureTypes.length + memberShift; i++) {
            if (foundVariable) {
                signatureNames.append(",");
            }
            LocalVariable lv = variables.get(i);
            if (lv != null) {
                SidratRegistry.instance().getRecorder().getLocalVariablesTracker().found(ctBehavior, lv);
                signatureNames.append("\"").append(lv.name).append("\"");
                foundVariable = true;
            } else {
                foundVariable = false;
            }
        }
        signatureNames.append("}");
        return signatureNames.toString();
    }
    */
    
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
