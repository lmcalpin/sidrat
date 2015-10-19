package com.sidrat.bytecode;

public class Parameters {
    public static final Parameters U2 = new Parameters(ParameterLength.U2);
    public static final Parameters S2 = new Parameters(ParameterLength.S2);
    public static final Parameters S4 = new Parameters(ParameterLength.S4);
    public static final Parameters ITSCOMPLICATED = new Parameters(); // TODO

    private ParameterLength[] parameterSize;
    private ParameterLength[] wideParameterSize;

    public Parameters(ParameterLength... parameterSize) {
        this.parameterSize = parameterSize;
    }

    public ParameterLength[] getParameters(InstructionContext ctx) {
        if (ctx.isWide()) {
            return wideParameterSize;
        }
        return parameterSize;
    }

    public Parameters ifWide(ParameterLength... wideParameterSize) {
        this.wideParameterSize = wideParameterSize;
        return this;
    }
}
