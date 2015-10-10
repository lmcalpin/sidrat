package com.sidrat.bytecode;

import static com.sidrat.bytecode.OperandValueType.ARRAYREF;
import static com.sidrat.bytecode.OperandValueType.INDEX;
import static com.sidrat.bytecode.OperandValueType.LENGTH;
import static com.sidrat.bytecode.OperandValueType.NULL;
import static com.sidrat.bytecode.OperandValueType.OBJECTREF;
import static com.sidrat.bytecode.OperandValueType.SIZE;
import static com.sidrat.bytecode.OperandValueType.VALUE;
import static com.sidrat.bytecode.OperandValueType.VALUE_WORD;
import static com.sidrat.bytecode.ParameterLength.S1;
import static com.sidrat.bytecode.ParameterLength.S2;
import static com.sidrat.bytecode.ParameterLength.U1;
import static com.sidrat.bytecode.ParameterLength.U2;

import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import com.sidrat.util.Logger;

import javassist.bytecode.Descriptor;

public enum Opcodes {
// @formatter:off
    AALOAD(50, new Pops(INDEX, ARRAYREF), new Pushes(VALUE)),
    AASTORE(83, new Pops(VALUE, INDEX, ARRAYREF)),
    ACONST_NULL(1, new Pushes(NULL)),
    ALOAD(25, new Pushes(OBJECTREF), new Parameters(U1).ifWide(U2)),
    ALOAD_0(42, ALOAD),
    ALOAD_1(43, ALOAD),
    ALOAD_2(44, ALOAD),
    ALOAD_3(45, ALOAD),
    ANEWARRAY(189, new Pops(SIZE), new Pushes(ARRAYREF), new Parameters(U2)),
    ARETURN(176, new Pops(OBJECTREF)),
    ARRAYLENGTH(190, new Pops(ARRAYREF), new Pushes(LENGTH)),
    ASTORE(58, new Pops(OBJECTREF), new Parameters(U1).ifWide(U2)),
    ASTORE_0(75, ASTORE),
    ASTORE_1(76, ASTORE),
    ASTORE_2(77, ASTORE),
    ASTORE_3(78, ASTORE),
    ATHROW(191, new Pops(OBJECTREF)),
    BALOAD(51, new Pops(INDEX, ARRAYREF), new Pushes(VALUE)),
    BASTORE(84, new Pops(VALUE, INDEX, ARRAYREF)),
    BIPUSH(16, new Pushes(VALUE), new Parameters(S1)),
    BREAKPOINT(202),
    CALOAD(52, new Pops(INDEX, ARRAYREF), new Pushes(VALUE)),
    CASTORE(85, new Pops(VALUE, INDEX, ARRAYREF)),
    CHECKCAST(192, Pops.OBJECTREF, Pushes.OBJECTREF, new Parameters(U2)),
    DCONST_0(14, Pushes.VALUEWORD_VALUEWORD),
    DCONST_1(15, Pushes.VALUEWORD_VALUEWORD),
    DLOAD(24, new Pushes(VALUE_WORD, VALUE_WORD), new Parameters(U1).ifWide(U2)),
    DLOAD_0(38, DLOAD),
    DLOAD_1(39, DLOAD),
    DLOAD_2(40, DLOAD),
    DLOAD_3(41, DLOAD),
    DMUL(107, new Pops(VALUE_WORD, VALUE_WORD, VALUE_WORD, VALUE_WORD), new Pushes(VALUE_WORD, VALUE_WORD)),
    DSTORE(57, new Pops(VALUE_WORD, VALUE_WORD), new Parameters(U1).ifWide(U2)),
    DSTORE_0(71, DSTORE),
    DSTORE_1(72, DSTORE),
    DSTORE_2(73, DSTORE),
    DSTORE_3(74, DSTORE),
    DALOAD(49, new Pops(INDEX, ARRAYREF), new Pushes(VALUE_WORD, VALUE_WORD)),
    DASTORE(82, new Pops(VALUE_WORD, VALUE_WORD, INDEX, ARRAYREF)),
    DUP(89, new Pushes(VALUE_WORD)) {
        @Override
        protected void pushOperands(Instruction i, Stack<OperandStackValue> stack) {
            OperandStackValue last = stack.pop();
            stack.push(last);
            stack.push(last);
        }
    },
    DUP2(92, new Pushes(VALUE_WORD, VALUE_WORD)) {
        @Override
        protected void pushOperands(Instruction i, Stack<OperandStackValue> stack) {
            OperandStackValue last = stack.pop();
            OperandStackValue first = stack.pop();
            stack.push(first);
            stack.push(last);
            stack.push(first);
            stack.push(last);
        }
    },
    FLOAD(23, new Pushes(VALUE), new Parameters(U1).ifWide(U2)),
    FLOAD_0(34, FLOAD),
    FLOAD_1(35, FLOAD),
    FLOAD_2(36, FLOAD),
    FLOAD_3(37, FLOAD),
    FMUL(106, Pops.VALUE_VALUE, Pushes.VALUE),
    FSTORE(56, new Pops(VALUE), new Parameters(U1).ifWide(U2)),
    FSTORE_0(67, FSTORE),
    FSTORE_1(68, FSTORE),
    FSTORE_2(69, FSTORE),
    FSTORE_3(70, FSTORE),
    FALOAD(48, new Pops(INDEX, ARRAYREF), new Pushes(VALUE)),
    FASTORE(81, new Pops(VALUE, INDEX, ARRAYREF)),
    FCONST_0(11, Pushes.VALUE),
    FCONST_1(12, Pushes.VALUE),
    FCONST_2(13, Pushes.VALUE),
    GETFIELD(180, new Pops(OBJECTREF), new Pushes(i -> {
        int methodRefIdx = i.getParameter(U2);
        String descriptor = i.getMethodInfo().getConstPool().getFieldrefType(methodRefIdx);
        if (Descriptor.dataSize(descriptor) == 2)
            return new OperandValueType[] { VALUE_WORD, VALUE_WORD };
        if (descriptor.startsWith("L"))
            return new OperandValueType[] { OBJECTREF };
        return new OperandValueType[] { VALUE };
    })),
    GETSTATIC(178, new Pushes(i -> {
        int methodRefIdx = i.getParameter(U2);
        String descriptor = i.getMethodInfo().getConstPool().getFieldrefType(methodRefIdx);
        if (Descriptor.dataSize(descriptor) == 2)
            return new OperandValueType[] { VALUE_WORD, VALUE_WORD };
        if (descriptor.startsWith("L"))
            return new OperandValueType[] { OBJECTREF };
        return new OperandValueType[] { VALUE };
    })),
    GOTO(167, Parameters.S2),
    I2L(133, Pops.VALUE, Pushes.VALUEWORD_VALUEWORD),
    ICONST_M1(2, Pushes.VALUE),
    ICONST_0(3, Pushes.VALUE),
    ICONST_1(4, Pushes.VALUE),
    ICONST_2(5, Pushes.VALUE),
    ICONST_3(6, Pushes.VALUE),
    ICONST_4(7, Pushes.VALUE),
    ICONST_5(8, Pushes.VALUE),
    IADD(96, Pops.VALUE_VALUE, Pushes.VALUE),
    IALOAD(46, new Pops(INDEX, ARRAYREF), new Pushes(VALUE)),
    IASTORE(79, new Pops(VALUE, INDEX, ARRAYREF)),
    IF_ICMPNE(160, Pops.VALUE_VALUE, Parameters.S2),
    IF_ICMPGE(162, Pops.VALUE_VALUE, Parameters.S2),
    IF_ICMPGT(163, Pops.VALUE_VALUE, Parameters.S2),
    IF_ICMPLT(161, Pops.VALUE_VALUE, Parameters.S2),
    IF_ICMPLE(164, Pops.VALUE_VALUE, Parameters.S2),
    IFNE(154, Pops.VALUE, Parameters.S2),
    IFEQ(153, Pops.VALUE, Parameters.S2),
    IFGT(157, Pops.VALUE, Parameters.S2),
    IFGE(156, Pops.VALUE, Parameters.S2),
    IFLT(155, Pops.VALUE, Parameters.S2),
    IFLE(158, Pops.VALUE, Parameters.S2),
    IFNULL(198, Pops.OBJECTREF, Parameters.S2),
    IFNONNULL(199, Pops.OBJECTREF, Parameters.S2),
    IINC(132, new Parameters(U1, S1).ifWide(U2, S2)),
    ILOAD(21, new Pushes(VALUE), new Parameters(U1).ifWide(U2)),
    ILOAD_0(26, ILOAD),
    ILOAD_1(27, ILOAD),
    ILOAD_2(28, ILOAD),
    ILOAD_3(29, ILOAD),
    IMUL(104, Pops.VALUE_VALUE, Pushes.VALUE),
    INVOKEDYNAMIC(186, Pops.STATICMETHODINVOCATION, Pushes.METHODINVOCATION, new Parameters(U2, U1, U1)), // last two U1s must be zero
    INVOKEINTERFACE(185, Pops.METHODINVOCATION, Pushes.METHODINVOCATION, new Parameters(U2, U1, U1)), // last U1 must be zero
    INVOKEVIRTUAL(182, Pops.METHODINVOCATION, Pushes.METHODINVOCATION, new Parameters(U2)),
    INVOKESPECIAL(183, Pops.METHODINVOCATION, Pushes.METHODINVOCATION, new Parameters(U2)),
    INVOKESTATIC(184, Pops.STATICMETHODINVOCATION, Pushes.METHODINVOCATION, new Parameters(U2)),
    IRETURN(172, Pops.VALUE),
    ISTORE(54, Pops.VALUE, new Parameters(U1).ifWide(U2)),
    ISTORE_0(59, ISTORE),
    ISTORE_1(60, ISTORE),
    ISTORE_2(61, ISTORE),
    ISTORE_3(62, ISTORE),
    LADD(97, new Pops(VALUE_WORD, VALUE_WORD, VALUE_WORD, VALUE_WORD), new Pushes(VALUE_WORD, VALUE_WORD)),
    LCONST_0(9, Pushes.VALUEWORD_VALUEWORD),
    LCONST_1(10, Pushes.VALUEWORD_VALUEWORD),
    LCMP(148, new Pops(VALUE_WORD, VALUE_WORD, VALUE_WORD, VALUE_WORD), new Pushes(VALUE)),
    LDC(18, new Pushes(VALUE), new Parameters(U1)),
    LDC_W(19, Pushes.VALUE, new Parameters(U2)),
    LDC2_W(20, Pushes.VALUEWORD_VALUEWORD, new Parameters(U2)),
    LLOAD(22, new Pushes(VALUE_WORD, VALUE_WORD), new Parameters(U1).ifWide(U2)),
    LLOAD_0(30, LLOAD),
    LLOAD_1(31, LLOAD),
    LLOAD_2(32, LLOAD),
    LLOAD_3(33, ILOAD),
    LSTORE(55, new Pops(VALUE_WORD, VALUE_WORD), new Parameters(U1).ifWide(U2)),
    LSTORE_0(63, LSTORE),
    LSTORE_1(64, LSTORE),
    LSTORE_2(65, LSTORE),
    LSTORE_3(66, LSTORE),
    LALOAD(47, new Pops(INDEX, ARRAYREF), new Pushes(VALUE_WORD, VALUE_WORD)),
    LASTORE(80, new Pops(VALUE_WORD, VALUE_WORD, INDEX, ARRAYREF)),
    NEW(187, new Pushes(OBJECTREF), new Parameters(U1)),
    NEWARRAY(188, new Pops(VALUE), new Pushes(OBJECTREF), new Parameters(U1)),
    NOP(0),
    POP(87, Pops.VALUE),
    POP2(88, Pops.VALUE_VALUE),
    PUTFIELD(181, new Pops(i -> {
        int methodRefIdx = i.getParameter(U1);
        String fieldName = i.getMethodInfo().getConstPool().getFieldrefName(methodRefIdx);
        // TODO: check if (fieldType is a long or double) and return new OperandValueType[] { VALUE_WORD, VALUE_WORD, OBJECTREF };
        return new OperandValueType[] { VALUE, OBJECTREF };
    })),
    PUTSTATIC(179, new Pops(i -> {
        int methodRefIdx = i.getParameter(U1);
        String fieldName = i.getMethodInfo().getConstPool().getFieldrefName(methodRefIdx);
        // TODO: check if (fieldType is a long or double) and return new OperandValueType[] { VALUE_WORD, VALUE_WORD };
        return new OperandValueType[] { VALUE };
    })),
    RET(169, new Parameters(U1).ifWide(U2)),
    RETURN(177), // void
    SALOAD(53, new Pops(INDEX, ARRAYREF), new Pushes(VALUE)),
    SASTORE(86, new Pops(VALUE, INDEX, ARRAYREF)),
    SIPUSH(17, Pushes.VALUE, new Parameters(S2)),
    WIDE(196)
    ;
 // @formatter:on

    private static final Logger logger = new Logger();

    private static final Map<Integer, Opcodes> BY_CODE = new TreeMap<>();

    static {
        for (Opcodes code : Opcodes.values()) {
            BY_CODE.put(code.code, code);
        }
    }

    private static final InstructionContext ctx = new InstructionContext(); // TODO
    private final int code;
    private Pops pops;
    private Pushes pushes;
    private Parameters parameters;

    private Opcodes(int code) {
        this.code = code;
    }

    // used for variable load/update opcodes that avoid the need for parameters (like ILOAD_0/1/2/3).
    private Opcodes(int code, Opcodes related) {
        this(code);
        this.pops = related.pops;
        this.pushes = related.pushes;
    }

    private Opcodes(int code, Parameters parameters) {
        this(code);
        this.parameters = parameters;
    }

    private Opcodes(int code, Pops pops) {
        this(code);
        this.pops = pops;
    }

    private Opcodes(int code, Pops pops, Parameters parameters) {
        this(code);
        this.pops = pops;
        this.parameters = parameters;
    }

    private Opcodes(int code, Pops pops, Pushes pushes) {
        this(code);
        this.pops = pops;
        this.pushes = pushes;
    }

    private Opcodes(int code, Pops pops, Pushes pushes, Parameters parameters) {
        this(code);
        this.pops = pops;
        this.pushes = pushes;
        this.parameters = parameters;
    }

    private Opcodes(int code, Pushes pushes) {
        this(code);
        this.pushes = pushes;
    }

    private Opcodes(int code, Pushes pushes, Parameters parameters) {
        this(code);
        this.pushes = pushes;
        this.parameters = parameters;
    }

    public static Opcodes fromMnemonic(String mnemonic) {
        String normalized = mnemonic.toUpperCase();
        try {
            if (Opcodes.valueOf(normalized) != null)
                return Opcodes.valueOf(normalized);
        } catch (IllegalArgumentException e) { // ignore
        }
        try {
            if (normalized.contains("_")) {
                String[] split = normalized.split("_");
                return Opcodes.valueOf(split[0]);
            }
        } catch (IllegalArgumentException e) { // ignore
        }
        throw new IllegalStateException("Implement " + mnemonic);
        // return null;
    }

    public static Opcodes fromOpcode(int code) {
        return BY_CODE.get(code);
    }

    public static boolean isFieldStore(int code) {
        Opcodes op = Opcodes.fromOpcode(code);
        return op == PUTSTATIC || op == PUTFIELD;
    }

    public static boolean isLocalVariableArrayUpdate(int code) {
        Opcodes op = Opcodes.fromOpcode(code);
        if (op == IASTORE || op == LASTORE || op == FASTORE || op == DASTORE || op == AASTORE || op == BASTORE || op == CASTORE || op == SASTORE)
            return true;
        return false;
    }

    public static boolean isLocalVariableLoad(int code) {
        Opcodes op = Opcodes.fromOpcode(code);
        if (op == ILOAD || op == LLOAD || op == FLOAD || op == DLOAD || op == ALOAD)
            return true;
        if (op == ILOAD_0 || op == ILOAD_1 || op == ILOAD_2 || op == ILOAD_3)
            return true;
        if (op == LLOAD_0 || op == LLOAD_1 || op == LLOAD_2 || op == LLOAD_3)
            return true;
        if (op == FLOAD_0 || op == FLOAD_1 || op == FLOAD_2 || op == FLOAD_3)
            return true;
        if (op == DLOAD_0 || op == DLOAD_1 || op == DLOAD_2 || op == DLOAD_3)
            return true;
        if (op == ALOAD_0 || op == ALOAD_1 || op == ALOAD_2 || op == ALOAD_3)
            return true;
        if (op == IALOAD || op == LALOAD || op == FALOAD || op == DALOAD || op == AALOAD || op == BALOAD || op == CALOAD || op == SALOAD)
            return true;
        return false;
    }

    public static boolean isLocalVariableUpdate(int code) {
        Opcodes op = Opcodes.fromOpcode(code);
        if (op == ISTORE || op == LSTORE || op == FSTORE || op == DSTORE || op == ASTORE)
            return true;
        if (op == ISTORE_0 || op == ISTORE_1 || op == ISTORE_2 || op == ISTORE_3)
            return true;
        if (op == LSTORE_0 || op == LSTORE_1 || op == LSTORE_2 || op == LSTORE_3)
            return true;
        if (op == FSTORE_0 || op == FSTORE_1 || op == FSTORE_2 || op == FSTORE_3)
            return true;
        if (op == DSTORE_0 || op == DSTORE_1 || op == DSTORE_2 || op == DSTORE_3)
            return true;
        if (op == ASTORE_0 || op == ASTORE_1 || op == ASTORE_2 || op == ASTORE_3)
            return true;
        if (isLocalVariableArrayUpdate(code))
            return true;
        if (op == IINC)
            return true;
        return false;
    }

    public int getCode() {
        return code;
    }

    protected Integer[] getParameters(Instruction i, ParameterLength[] len, Stack<OperandStackValue> stack) {
        return i.getParameters(parameters.getParameters(ctx));
    }

    private void popOperands(Instruction i, Stack<OperandStackValue> stack) {
        Stack<OperandStackValue> popped = new Stack<>();
        if (pops == null)
            return;
        for (OperandValueType unused : pops.getValues(i)) {
            if (stack.isEmpty()) {
                logger.warning("Stack was empty and we expected a value");
                continue;
            }
            logger.finest("  -- popped " + stack.peek().getType());
            popped.push(stack.pop());
        }
    }

    protected void pushOperands(Instruction i, Stack<OperandStackValue> stack) {
        if (pushes == null)
            return;
        for (OperandValueType type : pushes.getValues(i)) {
            logger.finest("  -- pushed " + type);
            stack.push(new OperandStackValue(type, i));
        }
    }

    public void simulate(Instruction i, Stack<OperandStackValue> stack) {
        popOperands(i, stack);
        pushOperands(i, stack);
        logger.finest("  -- stack: " + stack);
    }
}