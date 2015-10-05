package com.sidrat.bytecode;

public class Pushes {
    private OperandValueType[] valueTypes;

    public Pushes(OperandValueType... valueTypes) {
        this.valueTypes = valueTypes;
    }

    public OperandValueType[] getValues() {
        return valueTypes;
    }

}
