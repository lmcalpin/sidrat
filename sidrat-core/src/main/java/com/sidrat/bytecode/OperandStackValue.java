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
    private int[] values;

    public OperandStackValue(OperandValueType type, Instruction instruction) {
        this(type, instruction, (int[]) null);
    }

    public OperandStackValue(OperandValueType type, Instruction instruction, int value) {
        this.type = type;
        this.instruction = instruction;
        this.values = new int[] { value };
    }

    public OperandStackValue(OperandValueType type, Instruction instruction, int[] values) {
        this.type = type;
        this.instruction = instruction;
        this.values = values;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public int[] getParameters() {
        return values;
    }

    public OperandValueType getType() {
        return type;
    }
}
