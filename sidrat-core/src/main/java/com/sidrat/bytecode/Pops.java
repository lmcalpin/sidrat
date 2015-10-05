package com.sidrat.bytecode;

import java.util.function.Function;

public class Pops {
    private OperandValueType[] valueTypes;
    private Function<Instruction, OperandValueType[]> f;

    public Pops(Function<Instruction, OperandValueType[]> f) {
        this.f = f;
    }

    public Pops(OperandValueType... valueTypes) {
        this.valueTypes = valueTypes;
    }

    public OperandValueType[] getValues(Instruction i) {
        if (f != null) {
            return f.apply(i);
        }
        return valueTypes;
    }

}
