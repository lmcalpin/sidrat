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
    DLOAD(24, new Pushes(VALUE_WORD, VALUE_WORD), new Parameters(U1).ifWide(U2)),
    DLOAD_0(38, DLOAD),
    DLOAD_1(39, DLOAD),
    DLOAD_2(40, DLOAD),
    DLOAD_3(41, DLOAD),
    DSTORE(57, new Pops(VALUE_WORD, VALUE_WORD), new Parameters(U1).ifWide(U2)),
    DSTORE_0(71, DSTORE),
    DSTORE_1(72, DSTORE),
    DSTORE_2(73, DSTORE),
    DSTORE_3(74, DSTORE),
    DALOAD(49, new Pops(INDEX, ARRAYREF), new Pushes(VALUE_WORD, VALUE_WORD)),
    DASTORE(82, new Pops(VALUE_WORD, VALUE_WORD, INDEX, ARRAYREF)),
    FLOAD(23, new Pushes(VALUE), new Parameters(U1).ifWide(U2)),
    FLOAD_0(34, FLOAD),
    FLOAD_1(35, FLOAD),
    FLOAD_2(36, FLOAD),
    FLOAD_3(37, FLOAD),
    FSTORE(56, new Pops(VALUE), new Parameters(U1).ifWide(U2)),
    FSTORE_0(67, FSTORE),
    FSTORE_1(68, FSTORE),
    FSTORE_2(69, FSTORE),
    FSTORE_3(70, FSTORE),
    FALOAD(48, new Pops(INDEX, ARRAYREF), new Pushes(VALUE)),
    FASTORE(81, new Pops(VALUE, INDEX, ARRAYREF)),
    IALOAD(46, new Pops(INDEX, ARRAYREF), new Pushes(VALUE)),
    IASTORE(79, new Pops(VALUE, INDEX, ARRAYREF)),
    IINC(132, new Parameters(U1, S1).ifWide(U2, S2)),
    ILOAD(21, new Pushes(VALUE), new Parameters(U1).ifWide(U2)),
    ILOAD_0(26, ILOAD),
    ILOAD_1(27, ILOAD),
    ILOAD_2(28, ILOAD),
    ILOAD_3(29, ILOAD),
    ISTORE(54, new Pops(VALUE), new Parameters(U1).ifWide(U2)),
    ISTORE_0(59, ISTORE),
    ISTORE_1(60, ISTORE),
    ISTORE_2(61, ISTORE),
    ISTORE_3(62, ISTORE),
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
    NOP(0),
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
        return null;
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

    private void popOperands(Instruction i, Stack<OperandStackValue> stack) {
        Stack<OperandStackValue> popped = new Stack<>();
        if (pops == null)
            return;
        for (OperandValueType unused : pops.getValues(i)) {
            if (stack.isEmpty()) {
                logger.warning("Stack was empty and we expected a value");
                continue;
            }
            popped.push(stack.pop());
        }
    }

    private void pushOperands(Instruction i, int[] parameters, Stack<OperandStackValue> stack) {
        if (pushes == null)
            return;
        for (OperandValueType type : pushes.getValues()) {
            stack.push(new OperandStackValue(type, i, parameters));
        }
    }

    public void simulate(Instruction i, Stack<OperandStackValue> stack) {
        popOperands(i, stack);
        // extract parameters
        int[] paramValues = null;
        if (parameters != null) {
            paramValues = i.getParameters(parameters.getParameters(ctx));
        }
        pushOperands(i, paramValues, stack);
    }
}
