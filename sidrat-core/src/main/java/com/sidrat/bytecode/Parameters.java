package com.sidrat.bytecode;

public class Parameters {
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
