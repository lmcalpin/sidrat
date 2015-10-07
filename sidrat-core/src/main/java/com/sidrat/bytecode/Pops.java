package com.sidrat.bytecode;

import static com.sidrat.CheckedExceptionsAreStupidException.unchecked;

import java.util.function.Function;

import com.sidrat.util.Logger;

import javassist.ClassPool;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;

public class Pops {
    private static final Logger logger = new Logger();
    public static final Pops OBJECTREF = new Pops(OperandValueType.OBJECTREF);
    public static final Pops VALUE = new Pops(OperandValueType.VALUE);
    public static final Pops VALUE_VALUE = new Pops(OperandValueType.VALUE, OperandValueType.VALUE);
    public static final Pops METHODINVOCATION = new Pops(i -> {
        int methodRefIdx = i.getParameter(ParameterLength.U2);
        String methodName = getName(i, methodRefIdx);
        logger.finest("  -- popping in " + methodName + " @ " + i.getLineNumber());
        String descriptor = getDescriptor(i, methodRefIdx);
        ClassPool classPool = i.getBehavior().getDeclaringClass().getClassPool();
        int pops = unchecked(() -> Descriptor.getParameterTypes(descriptor, classPool)).length;
        OperandValueType[] types = new OperandValueType[pops + 1];
        types[0] = OperandValueType.OBJECTREF;
        for (int idx = 1; idx < pops; idx++) {
            types[idx] = OperandValueType.VALUE;
        }
        return types;
    });

    public static final Pops STATICMETHODINVOCATION = new Pops(i -> {
        int methodRefIdx = i.getParameter(ParameterLength.U2);
        String methodName = getName(i, methodRefIdx);
        logger.finest("  -- popping in " + methodName + " @ " + i.getLineNumber());
        String descriptor = getDescriptor(i, methodRefIdx);
        ClassPool classPool = i.getBehavior().getDeclaringClass().getClassPool();
        int pops = unchecked(() -> Descriptor.getParameterTypes(descriptor, classPool)).length;
        OperandValueType[] types = new OperandValueType[pops + 1];
        for (int idx = 0; idx < pops; idx++) {
            types[idx] = OperandValueType.VALUE;
        }
        return types;
    });
    private OperandValueType[] valueTypes;
    private Function<Instruction, OperandValueType[]> f;

    public Pops(Function<Instruction, OperandValueType[]> f) {
        this.f = f;
    }

    public Pops(OperandValueType... valueTypes) {
        this.valueTypes = valueTypes;
    }

    private static String getDescriptor(Instruction i, int methodRefIdx) {
        ConstPool constPool = i.getBehavior().getMethodInfo().getConstPool();
        if (constPool.getTag(methodRefIdx) == ConstPool.CONST_InterfaceMethodref)
            return constPool.getInterfaceMethodrefType(methodRefIdx);
        else if (constPool.getTag(methodRefIdx) == ConstPool.CONST_InvokeDynamic)
            return constPool.getInvokeDynamicType(methodRefIdx);
        return constPool.getMethodrefType(methodRefIdx);
    }

    private static String getName(Instruction i, int methodRefIdx) {
        ConstPool constPool = i.getBehavior().getMethodInfo().getConstPool();
        if (constPool.getTag(methodRefIdx) == ConstPool.CONST_InterfaceMethodref)
            return constPool.getInterfaceMethodrefName(methodRefIdx);
        else if (constPool.getTag(methodRefIdx) == ConstPool.CONST_InvokeDynamic)
            return "InvokeDynamic:" + methodRefIdx; // don't know the name at the moment, but we return this just for debugging purposes
        return constPool.getMethodrefName(methodRefIdx);
    }

    public OperandValueType[] getValues(Instruction i) {
        if (f != null) {
            return f.apply(i);
        }
        return valueTypes;
    }
}
