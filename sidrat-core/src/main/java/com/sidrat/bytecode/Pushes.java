package com.sidrat.bytecode;

import java.util.function.Function;

public class Pushes {
    public static final Pushes OBJECTREF = new Pushes(OperandValueType.OBJECTREF);
    public static final Pushes VALUE = new Pushes(OperandValueType.VALUE);
    public static final Pushes VALUEWORD2 = new Pushes(OperandValueType.VALUE_WORD, OperandValueType.VALUE_WORD);
    public static final Pushes METHODINVOCATION = new Pushes(i -> {
        String descriptor = i.getMethodInfo().getDescriptor();
        if (descriptor.equals("V"))
            return null;
        return new OperandValueType[] { OperandValueType.VALUE };
    });
    private OperandValueType[] valueTypes;
    private Function<Instruction, OperandValueType[]> f;

    public Pushes(Function<Instruction, OperandValueType[]> f) {
        this.f = f;
    }

    public Pushes(OperandValueType... valueTypes) {
        this.valueTypes = valueTypes;
    }

    public OperandValueType[] getValues(Instruction i) {
        if (f != null) {
            return f.apply(i);
        }
        return valueTypes;
    }

}
