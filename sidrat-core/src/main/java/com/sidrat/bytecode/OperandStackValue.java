package com.sidrat.bytecode;

/**
 * A value that will be pushed on to the stack. This is tracked at instrumentation time while byteweaving the methods that we intend to
 * track, so we can not know the actual values until runtime. Instead, we track variable slots, constants, or field references.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class OperandStackValue {
    private OperandValueType type;
    private Instruction instruction;

    public OperandStackValue(OperandValueType type, Instruction instruction) {
        this.type = type;
        this.instruction = instruction;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public OperandValueType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.name();
    }
}
