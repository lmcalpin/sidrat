package com.sidrat.instrument;

import static javassist.bytecode.Opcode.AALOAD;
import static javassist.bytecode.Opcode.AASTORE;
import static javassist.bytecode.Opcode.ALOAD;
import static javassist.bytecode.Opcode.ALOAD_0;
import static javassist.bytecode.Opcode.ALOAD_1;
import static javassist.bytecode.Opcode.ALOAD_2;
import static javassist.bytecode.Opcode.ALOAD_3;
import static javassist.bytecode.Opcode.ASTORE;
import static javassist.bytecode.Opcode.ASTORE_0;
import static javassist.bytecode.Opcode.ASTORE_1;
import static javassist.bytecode.Opcode.ASTORE_2;
import static javassist.bytecode.Opcode.ASTORE_3;
import static javassist.bytecode.Opcode.BALOAD;
import static javassist.bytecode.Opcode.BASTORE;
import static javassist.bytecode.Opcode.CALOAD;
import static javassist.bytecode.Opcode.CASTORE;
import static javassist.bytecode.Opcode.DALOAD;
import static javassist.bytecode.Opcode.DASTORE;
import static javassist.bytecode.Opcode.DLOAD;
import static javassist.bytecode.Opcode.DLOAD_0;
import static javassist.bytecode.Opcode.DLOAD_1;
import static javassist.bytecode.Opcode.DLOAD_2;
import static javassist.bytecode.Opcode.DLOAD_3;
import static javassist.bytecode.Opcode.DSTORE;
import static javassist.bytecode.Opcode.DSTORE_0;
import static javassist.bytecode.Opcode.DSTORE_1;
import static javassist.bytecode.Opcode.DSTORE_2;
import static javassist.bytecode.Opcode.DSTORE_3;
import static javassist.bytecode.Opcode.FALOAD;
import static javassist.bytecode.Opcode.FASTORE;
import static javassist.bytecode.Opcode.FLOAD;
import static javassist.bytecode.Opcode.FLOAD_0;
import static javassist.bytecode.Opcode.FLOAD_1;
import static javassist.bytecode.Opcode.FLOAD_2;
import static javassist.bytecode.Opcode.FLOAD_3;
import static javassist.bytecode.Opcode.FSTORE;
import static javassist.bytecode.Opcode.FSTORE_0;
import static javassist.bytecode.Opcode.FSTORE_1;
import static javassist.bytecode.Opcode.FSTORE_2;
import static javassist.bytecode.Opcode.FSTORE_3;
import static javassist.bytecode.Opcode.IALOAD;
import static javassist.bytecode.Opcode.IASTORE;
import static javassist.bytecode.Opcode.IINC;
import static javassist.bytecode.Opcode.ILOAD;
import static javassist.bytecode.Opcode.ILOAD_0;
import static javassist.bytecode.Opcode.ILOAD_1;
import static javassist.bytecode.Opcode.ILOAD_2;
import static javassist.bytecode.Opcode.ILOAD_3;
import static javassist.bytecode.Opcode.ISTORE;
import static javassist.bytecode.Opcode.ISTORE_0;
import static javassist.bytecode.Opcode.ISTORE_1;
import static javassist.bytecode.Opcode.ISTORE_2;
import static javassist.bytecode.Opcode.ISTORE_3;
import static javassist.bytecode.Opcode.LALOAD;
import static javassist.bytecode.Opcode.LASTORE;
import static javassist.bytecode.Opcode.LLOAD;
import static javassist.bytecode.Opcode.LLOAD_0;
import static javassist.bytecode.Opcode.LLOAD_1;
import static javassist.bytecode.Opcode.LLOAD_2;
import static javassist.bytecode.Opcode.LLOAD_3;
import static javassist.bytecode.Opcode.LSTORE;
import static javassist.bytecode.Opcode.LSTORE_0;
import static javassist.bytecode.Opcode.LSTORE_1;
import static javassist.bytecode.Opcode.LSTORE_2;
import static javassist.bytecode.Opcode.LSTORE_3;
import static javassist.bytecode.Opcode.PUTFIELD;
import static javassist.bytecode.Opcode.PUTSTATIC;
import static javassist.bytecode.Opcode.SALOAD;
import static javassist.bytecode.Opcode.SASTORE;

import java.util.Stack;

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
import javassist.bytecode.Mnemonic;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;

/**
 * A 
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class Instruction {
    private static final Logger logger = new Logger();
    
    private int pos;
    private int next;

    private final int lineNumber;
    private final int op;
    private final Instruction prev;
    private final Integer param;
    private final String mnemonic;
    private CodeAttribute ca;
    private final CtBehavior ctBehavior;
    private final CtClass cc;
    
    public Instruction(int pos, int lineNumber, int op, int next, Instruction prev, CodeIterator ci, CtBehavior behavior) {
        this.pos = pos;
        this.op = op;
        this.next = next;
        this.lineNumber = lineNumber;
        this.prev = prev;
        Integer paramValue = null;
        if (isFieldStore())
            paramValue = ci.u16bitAt(pos + 1);
        ca = ci.get();
        this.param = paramValue;
        this.mnemonic = Mnemonic.OPCODE[op];
        this.ctBehavior = behavior;
        this.cc = behavior.getDeclaringClass();
    }
    
    public int getOpcode() { return op; }
    public int getPosition() { return pos; }
    public int getLineNumber() { return lineNumber; }
    public Integer getParameter() { return param; }
    
    public Instruction prev() { return prev; }
    public Instruction prev(int steps) {
        Instruction p = prev;
        int i = 1;
        while (p != null && i < steps) {
            p = p.prev();
            i++;
        }
        return p;
    }
    
    public String getMnemonic() { return mnemonic; }
    
    public boolean isFieldStore() {
        return op == PUTSTATIC || op == PUTFIELD;
    }

    public boolean isLocalVariableUpdate() {
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
        if (isLocalVariableArrayUpdate())
            return true;
        if (op == IINC)
            return true;
        return false;
    }
    
    public boolean isLocalVariableArrayUpdate() {
        if (op == IASTORE || op == LASTORE || op == FASTORE || op == DASTORE || op == AASTORE || op == BASTORE || op == CASTORE || op == SASTORE)
            return true;
        return false;
    }
    
    public boolean isLocalVariableLoad() {
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
        if (op == IALOAD || op == LALOAD || op == FALOAD || op == DALOAD || op == AALOAD || op == BALOAD || op == CALOAD || op == SALOAD)
            return true;
        return false;
    }
    
    public void insert(Stack<Instruction> instructions, String src) throws BadBytecode, CannotCompileException, NotFoundException {
        try {
            logger.finer("compiling \"" + src + "\" for " + lineNumber);
            CodeIterator ci = ca.iterator();
            ci.move(next);
            byte[] code = compile(this, src);
            ci.insert(code);
            int offset = code.length;
            for (Instruction instruction : instructions) {
                if (instruction.pos > pos) {
                    instruction.pos += offset;
                    instruction.next += offset;
                }
            }
        } catch (CannotCompileException e) {
            logger.severe("Failed to compile [" + src + "] at line number [" + lineNumber + "]");
        }
    }

    private byte[] compile(Instruction sf, String src) throws CannotCompileException, BadBytecode, NotFoundException {
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

    @Override
    public String toString() {
        return lineNumber + ": " + mnemonic;
    }
}