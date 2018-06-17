package at.jku.ssw.java.bytecode.reducer.utils.javassist;

import java.util.Set;

import static javassist.bytecode.Opcode.*;

/**
 * Javassist helpers directly related to bytecode level manipulation.
 */
public final class Code {

    /**
     * Contains opcodes for instructions that put one value onto the stack.
     */
    private static final Set<Integer> plus1;

    /**
     * Contains opcodes for instructions that put two values onto the stack.
     */
    private static final Set<Integer> plus2;

    /**
     * Contains opcodes that remove one value from the stack.
     */
    private static final Set<Integer> min1;

    /**
     * Contains opcodes that remove two values from the stack.
     */
    private static final Set<Integer> min2;

    /**
     * Contains opcodes that remove three values from the stack.
     */
    private static final Set<Integer> min3;


    /**
     * Contains opcodes that do not change the stack level
     * (either remove the same amount of values that they put onto
     * it or do not touch it at all).
     */
    private static final Set<Integer> neutral;

    /**
     * Contains opcodes that remove or push a variable number of values
     * from / onto the stack.
     */
    private static final Set<Integer> special;

    static {
        plus1 = Set.of(
                ACONST_NULL,
                ALOAD,
                ALOAD_0,
                ALOAD_1,
                ALOAD_2,
                ALOAD_3,
                BIPUSH,
                DLOAD,
                DLOAD_0,
                DLOAD_1,
                DLOAD_2,
                DLOAD_3,
                DUP,
                DUP_X1,
                DUP_X2,
                FCONST_0,
                FCONST_1,
                FCONST_2,
                FLOAD,
                FLOAD_0,
                FLOAD_1,
                FLOAD_2,
                FLOAD_3,
                ICONST_M1,
                ICONST_0,
                ICONST_1,
                ICONST_2,
                ICONST_3,
                ICONST_4,
                ICONST_5,
                ILOAD,
                ILOAD_0,
                ILOAD_1,
                ILOAD_2,
                ILOAD_3,
                JSR,
                JSR_W,
                LCONST_0,
                LCONST_1,
                LDC,
                LDC_W,
                LDC2_W,
                LLOAD,
                LLOAD_0,
                LLOAD_1,
                LLOAD_2,
                LLOAD_3,
                NEW
        );

        plus2 = Set.of(
                DUP2,
                DUP2_X1,
                DUP2_X2
        );

        min1 = Set.of(
                AALOAD,
                ASTORE,
                ASTORE_0,
                ASTORE_1,
                ASTORE_2,
                ASTORE_3,
                BALOAD,
                CALOAD,
                DADD,
                DALOAD,
                DCMPG,
                DCMPL,
                DMUL,
                DREM,
                DSTORE,
                DSTORE_0,
                DSTORE_1,
                DSTORE_2,
                DSTORE_3,
                DSUB,
                FADD,
                FALOAD,
                FCMPG,
                FCMPL,
                FDIV,
                FMUL,
                FREM,
                FSTORE,
                FSTORE_0,
                FSTORE_1,
                FSTORE_2,
                FSTORE_3,
                FSUB,
                GETSTATIC,
                IADD,
                IALOAD,
                IAND,
                IDIV,
                IFEQ,
                IFGE,
                IFGT,
                IFLE,
                IFLT,
                IFNE,
                IFNONNULL,
                IFNULL,
                IMUL,
                IOR,
                IREM,
                ISHL,
                ISHR,
                ISTORE,
                ISTORE_0,
                ISTORE_1,
                ISTORE_2,
                ISTORE_3,
                ISUB,
                IUSHR,
                IXOR,
                LADD,
                LALOAD,
                LAND,
                LCMP,
                LDIV,
                LMUL,
                LOR,
                LREM,
                LSHL,
                LSHR,
                LSTORE,
                LSTORE_0,
                LSTORE_1,
                LSTORE_2,
                LSTORE_3,
                LSUB,
                LUSHR,
                LXOR,
                MONITORENTER,
                MONITOREXIT,
                POP,
                PUTSTATIC,
                SALOAD,
                SIPUSH
        );

        min2 = Set.of(
                IF_ACMPEQ,
                IF_ACMPNE,
                IF_ICMPEQ,
                IF_ICMPGE,
                IF_ICMPGT,
                IF_ICMPLE,
                IF_ICMPLT,
                IF_ICMPNE,
                POP2,
                PUTFIELD
        );

        min3 = Set.of(
                AASTORE,
                BASTORE,
                CASTORE,
                DASTORE,
                FASTORE,
                IASTORE,
                LASTORE,
                SASTORE
        );

        neutral = Set.of(
                ANEWARRAY,
                ARRAYLENGTH,
                CHECKCAST,
                D2F,
                D2I,
                D2L,
                DNEG,
                F2D,
                F2I,
                F2L,
                FNEG,
                GETFIELD,
                I2B,
                I2C,
                I2D,
                I2F,
                I2L,
                I2S,
                IINC,
                INEG,
                INSTANCEOF,
                L2D,
                L2F,
                L2I,
                LNEG,
                NEWARRAY,
                NOP,
                SWAP
        );

        special = Set.of(
                ARETURN,
                ATHROW,
                DRETURN,
                FRETURN,
                GOTO,
                GOTO_W,
                INVOKEDYNAMIC,
                INVOKEINTERFACE,
                INVOKESPECIAL,
                INVOKEVIRTUAL,
                IRETURN,
                LOOKUPSWITCH, // removes 1 but jumps to address
                LRETURN,
                MULTIANEWARRAY,
                RET,
                RETURN,
                TABLESWITCH,
                WIDE
        );

    }

    private Code() {
    }

    /**
     * Checks whether the operation indicated by the given {@link javassist.bytecode.Opcode}
     * increases the stack size.
     *
     * @param opcode The opcode of the current operation
     * @return {@code true} if the operation puts values on the stack;
     * {@code false} if the stack is reduced or invariant
     */
    public static boolean isStackPush(int opcode) {
        return getStackLevelChange(opcode) > 0;
    }

    /**
     * Checks whether the operation indicated by the given {@link javassist.bytecode.Opcode}
     * decreases the stack size.
     *
     * @param opcode The opcode of the current operation
     * @return {@code true} if the operation removes values from the stack;
     * {@code false} if the stack is increased or invariant
     */
    public static boolean isStackPop(int opcode) {
        return getStackLevelChange(opcode) < 0;
    }

    /**
     * Checks whether the given opcode represents an instruction
     * that either involves a context switch / jump or puts / pops a variable
     * number of values onto / from the stack.
     *
     * @param opcode The opcode of the current operation
     * @return {@code true} if the operation does not change the stack size
     * by a static amount or switches the execution context; {@code false}
     * otherwise
     */
    public static boolean isSpecial(int opcode) {
        return special.contains(opcode);
    }

    /**
     * Checks whether the instruction given by the opcode does not change
     * the stack size.
     *
     * @param opcode The opcode of the current operation.
     * @return {@code true} if the operation does not change the stack size;
     * {@code false} otherwise
     */
    public static boolean isNeutral(int opcode) {
        return neutral.contains(opcode);
    }

    /**
     * Calculates the change in stack level, that the operation indicated by
     * the given {@link javassist.bytecode.Opcode} implies.
     *
     * @param opcode The opcode of the current operation
     * @return a positive value if the stack is increased, a negative value
     * if the stack is reduced; 0 if the stack remains invariant
     */
    public static int getStackLevelChange(int opcode) {
        if (plus1.contains(opcode))
            return 1;
        if (plus2.contains(opcode))
            return 2;
        if (min1.contains(opcode))
            return -1;
        if (min2.contains(opcode))
            return -2;
        if (min3.contains(opcode))
            return -3;

        return 0;
    }
}
