package com.sidrat.bytecode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sidrat.util.Logger;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;

/**
 * An opcode instruction read while instrumenting a method.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class Instruction {
    private static final Logger logger = new Logger();

    private CodeAttribute ca;
    private final CtClass cc;
    private final CtBehavior ctBehavior;
    private final int lineNumber;
    private final String mnemonic;
    private final int op;
    private final Instruction prev;
    private Instruction next;
    private int pos; // program counter
    private int nextPos; // program counter

    public Instruction(int pos, int lineNumber, int op, int nextPos, Instruction prev, CodeAttribute ca, CtBehavior behavior) {
        this.pos = pos;
        this.op = op;
        this.nextPos = nextPos;
        this.lineNumber = lineNumber;
        this.prev = prev;
        this.ca = ca;
        this.mnemonic = Mnemonic.OPCODE[op];
        this.ctBehavior = behavior;
        this.cc = behavior.getDeclaringClass();
    }

    public static List<Instruction> analyze(CtBehavior ctBehavior, CodeAttribute ca) throws BadBytecode {
        List<Instruction> instructions = new ArrayList<>();
        MethodInfo methodInfo = ctBehavior.getMethodInfo();
        Instruction last = null;
        CodeIterator ci = ca.iterator();
        while (ci.hasNext()) {
            int pos = ci.next();
            int op = ci.byteAt(pos);
            int next = ci.lookAhead();
            int thisLineNumber = methodInfo.getLineNumber(pos);
            Instruction instruction = new Instruction(pos, thisLineNumber, op, next, last, ca, ctBehavior);
            if (last != null)
                last.next = instruction;
            last = instruction;
            instructions.add(instruction);
        }
        return instructions;
    }

    private byte[] compile(Instruction sf, String src) throws CannotCompileException, BadBytecode, NotFoundException {
        Javac jv = new Javac(cc);
        try {
            int pc = sf.nextPos;
            if (sf.next.op == Opcode.RETURN)
                pc = sf.pos;
            jv.recordLocalVariables(ca, pc);
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

    public CtBehavior getBehavior() {
        return ctBehavior;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public MethodInfo getMethodInfo() {
        return ctBehavior.getMethodInfo();
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public int getOpcode() {
        return op;
    }

    public Integer getParameter(ParameterLength len) {
        if (len == null)
            return null;
        return getParameters(new ParameterLength[] { len })[0];
    }

    public Integer[] getParameters(ParameterLength[] len) {
        if (len == null)
            return null;
        CodeIterator ci = ca.iterator();
        Integer[] values = new Integer[len.length];
        int idx = 0;
        int parameterPos = pos + 1;
        try {
            for (ParameterLength l : len) {
                switch (l) {
                case U1:
                    {
                        values[idx++] = ci.byteAt(parameterPos);
                        parameterPos += 1;
                    }
                    break;
                case U2:
                    {
                        values[idx++] = ci.u16bitAt(parameterPos);
                        parameterPos += 2;
                    }
                    break;
                case S1:
                    {
                        values[idx++] = ci.byteAt(parameterPos);
                        parameterPos += 1;
                    }
                    break;
                case S2:
                    {
                        values[idx++] = ci.s16bitAt(parameterPos);
                        parameterPos += 2;
                    }
                    break;
                case S4:
                    {
                        values[idx++] = ci.s32bitAt(parameterPos);
                        parameterPos += 4;
                    }
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return values;
    }

    public int getPosition() {
        return pos;
    }

    public void insert(Collection<Instruction> instructions, String src) throws BadBytecode, CannotCompileException, NotFoundException {
        try {
            logger.finer("compiling \"" + src + "\" for " + lineNumber);
            CodeIterator ci = ca.iterator();
            ci.move(nextPos);
            byte[] code = compile(this, src);
            ci.insert(code);
            int offset = code.length;
            for (Instruction instruction : instructions) {
                if (instruction.pos > pos) {
                    instruction.pos += offset;
                    instruction.nextPos += offset;
                }
            }
        } catch (CannotCompileException e) {
            logger.severe("Failed to compile [" + src + "] at line number [" + lineNumber + "]", e);
        }
    }

    public boolean isFieldStore() {
        return Opcodes.isFieldStore(op);
    }

    public boolean isLocalVariableArrayUpdate() {
        return Opcodes.isLocalVariableArrayUpdate(op);
    }

    public boolean isLocalVariableLoad() {
        return Opcodes.isLocalVariableLoad(op);

    }

    public boolean isLocalVariableUpdate() {
        return Opcodes.isLocalVariableUpdate(op);
    }

    public Instruction prev() {
        return prev;
    }

    public Instruction prev(int steps) {
        Instruction p = prev;
        int i = 1;
        while (p != null && i < steps) {
            p = p.prev();
            i++;
        }
        return p;
    }

    @Override
    public String toString() {
        return lineNumber + ": " + mnemonic;
    }
}