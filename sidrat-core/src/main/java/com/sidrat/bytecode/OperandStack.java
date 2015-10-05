package com.sidrat.bytecode;

import java.util.Stack;

import javassist.bytecode.CodeAttribute;

public class OperandStack {
    private final CodeAttribute ca;
    private final Stack<OperandStackValue> stack = new Stack<>();

    public OperandStack(CodeAttribute ca) {
        this.ca = ca;
    }

    public OperandStackValue peek() {
        if (stack.isEmpty())
            return null;
        return stack.peek();
    }

    public void simulate(Instruction i) {
        Opcodes opcode = Opcodes.fromMnemonic(i.getMnemonic());
        if (opcode != null) {
            opcode.simulate(i, stack);
        }
    }
}
