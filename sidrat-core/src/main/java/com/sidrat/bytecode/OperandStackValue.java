package com.sidrat.bytecode;

import com.sidrat.event.tracking.LocalVariable;

/**
 * A value that will be pushed on to the stack. This is tracked at instrumentation time while byteweaving the methods that we intend to
 * track, so we can not know the actual values until runtime. Instead, we track variable slots, constants, or field references.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class OperandStackValue {
    private final OperandValueType type;
    private final Instruction instruction;

    // if the stack value is a local variable, remember its name
    private LocalVariable localVariable;

    public OperandStackValue(OperandValueType type, Instruction instruction) {
        this.type = type;
        this.instruction = instruction;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public LocalVariable getLocalVariable() {
        return localVariable;
    }

    public OperandValueType getType() {
        return type;
    }

    public void setLocalVariable(LocalVariable localVariable) {
        this.localVariable = localVariable;
    }

    @Override
    public String toString() {
        return type.name();
    }

}
