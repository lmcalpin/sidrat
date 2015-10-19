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

    public OperandStackValue peek2() {
        if (stack.isEmpty())
            return null;
        if (stack.size() == 1)
            return null;
        return stack.get(stack.size() - 2);
    }

    public OperandStackValue peek3() {
        if (stack.isEmpty())
            return null;
        if (stack.size() < 3)
            return null;
        return stack.get(stack.size() - 3);
    }

    public OperandStackValue peek4() {
        if (stack.isEmpty())
            return null;
        if (stack.size() < 4)
            return null;
        return stack.get(stack.size() - 4);
    }

    public void simulate(Instruction i) {
        Opcodes opcode = Opcodes.fromMnemonic(i.getMnemonic());
        if (opcode != null) {
            opcode.simulate(i, stack);
        }
    }

    public int size() {
        return stack.size();
    }
}
