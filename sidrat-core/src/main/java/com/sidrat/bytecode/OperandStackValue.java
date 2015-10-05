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
    private Integer[] values;

    public OperandStackValue(OperandValueType type, Instruction instruction) {
        this(type, instruction, (Integer[]) null);
    }

    public OperandStackValue(OperandValueType type, Instruction instruction, Integer value) {
        this.type = type;
        this.instruction = instruction;
        this.values = new Integer[] { value };
    }

    public OperandStackValue(OperandValueType type, Instruction instruction, Integer[] values) {
        this.type = type;
        this.instruction = instruction;
        this.values = values;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public Integer[] getParameters() {
        return values;
    }

    public OperandValueType getType() {
        return type;
    }
}
