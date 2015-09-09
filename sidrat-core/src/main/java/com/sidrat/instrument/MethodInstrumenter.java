package com.sidrat.instrument;

import com.sidrat.SidratProcessingException;
import com.sidrat.SidratRegistry;

import bytecodeparser.analysis.LocalVariable;
import bytecodeparser.analysis.decoders.DecodedFieldOp;
import bytecodeparser.analysis.decoders.DecodedLocalVariableOp;
import bytecodeparser.analysis.stack.StackAnalyzer;
import bytecodeparser.analysis.stack.StackAnalyzer.Frame;
import bytecodeparser.analysis.stack.StackAnalyzer.Frames;
import bytecodeparser.analysis.stack.StackAnalyzer.Frames.FrameIterator;
import bytecodeparser.analysis.stack.ValueFromLocalVariable;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;

public class MethodInstrumenter {
    private final CtBehavior ctBehavior;
    private MethodInfo methodInfo;
    private CtClass cc;
    private int lineNumber = -1;
    private final String className, methodName;

    public MethodInstrumenter(CtBehavior ctBehavior) {
        this.ctBehavior = ctBehavior;
        this.cc = ctBehavior.getDeclaringClass();
        this.methodInfo = ctBehavior.getMethodInfo();
        this.className = cc.getName();
        this.methodName = ctBehavior.getMethodInfo().getName();
    }

    public void instrument() {
        try {
            CodeAttribute codeAttr = methodInfo.getCodeAttribute();
            if (codeAttr == null)
                throw new CannotCompileException("Error instrumenting " + className + "; no line number info");
            LineNumberAttribute ainfo = (LineNumberAttribute) codeAttr.getAttribute(LineNumberAttribute.tag);
            StackAnalyzer parser = new StackAnalyzer(ctBehavior);
            Frames frames = parser.analyze();
            FrameIterator iterator = frames.iterator();
            int flags = ctBehavior.getMethodInfo().getAccessFlags();
            while (iterator.hasNext()) {
                Frame frame = iterator.next();
                if (!frame.isAccessible) {
                    continue;
                }
                int thisLineNumber = methodInfo.getLineNumber(frame.index);
                if (thisLineNumber != lineNumber && !iterator.isLast()) {
                    this.lineNumber = thisLineNumber;
                    String src = "com.sidrat.event.SidratCallback.exec(" + lineNumber + ");";
                    compile(iterator, frame.index, src, true);
                }
                // track assignments to fields
                if (frame.decodedOp instanceof DecodedFieldOp) {
                    DecodedFieldOp op = (DecodedFieldOp) frame.decodedOp;
                    if (!op.isRead()) {
                        String fieldName = op.context.behavior.getMethodInfo().getConstPool().getFieldrefName(op.getMethodRefIndex());
                        String fieldType = op.getDescriptor();
                        boolean staticField = op.op.getCode() == Opcode.PUTSTATIC;
                        if (staticField) {
                            int constantPoolIndex = op.parameterValues[0];
                            String refClassName = codeAttr.getConstPool().getFieldrefClassName(constantPoolIndex);
                            String staticFieldRef = refClassName.replaceAll("\\$", ".") + "." + fieldName;
                            String src = "com.sidrat.event.SidratCallback.fieldChanged(" + refClassName + ".class, " + staticFieldRef + ",\"" + fieldName + "\");";
                            compile(iterator, iterator.lookAhead().index, src, true);
                        } else {
                            String localVariable = ((ValueFromLocalVariable)frame.stackBefore.stack.get(1)).localVariable.name;
                            String src = "com.sidrat.event.SidratCallback.fieldChanged(" + localVariable + ", " + localVariable + "." + fieldName + ",\"" + fieldName + "\");";
                            compile(iterator, iterator.lookAhead().index, src, true);
                        }
                    }
                // track assignments to local variables
                } else if (frame.decodedOp instanceof DecodedLocalVariableOp) {
                    DecodedLocalVariableOp op = (DecodedLocalVariableOp) frame.decodedOp;
                    if (!op.load) {
                        if (op.localVariable != null) {
                            String localVariable = op.localVariable.name;
                            SidratRegistry.instance().getRecorder().getLocalVariablesTracker().found(ctBehavior, op.localVariable);
                            String src = "com.sidrat.event.SidratCallback.variableChanged(" + localVariable + ",\"" + op.localVariable.name + "\");";
                            compile(iterator, iterator.lookAhead().index, src, true);
                        }
                    }
                }
            }
            int firstLineNumber = ainfo.lineNumber(0);
            int lastLineNumber = ainfo.lineNumber(ainfo.tableLength()-1);
            String signatureNames = getLocalVariablesFromMethodSignature(parser);
            if ((flags & AccessFlag.STATIC)  != 0 || ctBehavior.getMethodInfo().isConstructor()) {
                ctBehavior.insertBefore("com.sidrat.event.SidratCallback.enter(\"" + className + "\",\""
                        + methodName + "\", " + signatureNames + ", $args);");
            } else {
                ctBehavior.insertBefore("com.sidrat.event.SidratCallback.enter($0, \"" + className + "\",\""
                        + methodName + "\", " + signatureNames + ", $args);");
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

    private void compile(FrameIterator fi, int index, String src, boolean after) throws BadBytecode, CannotCompileException, NotFoundException {
        CodeAttribute ca = methodInfo.getCodeAttribute();
        byte[] code = compile(index, src);
        fi.insert(code, after);
        ca.setMaxStack(ca.computeMaxStack());
    }

    public byte[] compile(int idx, String src) throws CannotCompileException, BadBytecode, NotFoundException {
        CodeAttribute ca = methodInfo.getCodeAttribute();
        if (ca == null)
            throw new CannotCompileException("no method body");

        Javac jv = new Javac(cc);
        try {
            jv.recordLocalVariables(ca, idx);
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

    private String getLocalVariablesFromMethodSignature(StackAnalyzer parser) throws NotFoundException {
        int memberShift = Modifier.isStatic(ctBehavior.getModifiers()) ? 0 : 1;
        CtClass[] signatureTypes = ctBehavior.getParameterTypes();
        if (signatureTypes.length == 0)
            return "null";
        StringBuilder signatureNames = new StringBuilder("new java.lang.String[] {");
        for (int i = memberShift; i < signatureTypes.length + memberShift; i++) {
            if (i > memberShift) {
                signatureNames.append(",");
            }

            LocalVariable lv = parser.context.localVariables.get(i);
            SidratRegistry.instance().getRecorder().getLocalVariablesTracker().found(ctBehavior, lv);

            signatureNames.append("\"").append(lv.name).append("\"");
        }
        signatureNames.append("}");
        return signatureNames.toString();
    }
}
