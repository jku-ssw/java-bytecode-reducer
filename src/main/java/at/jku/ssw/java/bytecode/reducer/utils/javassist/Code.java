package at.jku.ssw.java.bytecode.reducer.utils.javassist;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Descriptor;
import javassist.bytecode.analysis.Analyzer;
import javassist.bytecode.analysis.Type;

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

    /**
     * Contains opcodes that clear the stack (or most of it).
     */
    private static final Set<Integer> clear;

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
                GETSTATIC,
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
                NEW,
                SIPUSH
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
                LOOKUPSWITCH,
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
                TABLESWITCH
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

        clear = Set.of(
                ARETURN,
                ATHROW,
                DRETURN,
                FRETURN,
                IRETURN,
                LRETURN,
                RETURN
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
                GOTO,
                GOTO_W,
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
                RET,
                SWAP
        );

        special = Set.of(
                ARETURN,
                ATHROW,
                DRETURN,
                DUP2,
                DUP2_X1,
                DUP2_X2,
                FRETURN,
                GOTO,
                GOTO_W,
                INVOKEDYNAMIC,
                INVOKEINTERFACE,
                INVOKESPECIAL,
                INVOKESTATIC,
                INVOKEVIRTUAL,
                IRETURN,
                JSR,                // place address on stack but also jumps
                JSR_W,              // same as above
                LOOKUPSWITCH,       // removes 1 but jumps to address
                LRETURN,
                MULTIANEWARRAY,
                POP2,
                RET,
                RETURN,
                TABLESWITCH,
                WIDE
        );

    }

    private Code() {
    }

    /**
     * Determines whether the top of the stack at the current position in the
     * method is a double word length value (double or long).
     *
     * @param method The current method
     * @param index  The index of the position to analyze
     * @return {@code true} if the stack top is of size double word,
     * {@code false} otherwise
     * @throws BadBytecode if the current bytecode is flawed
     */
    private static boolean stackTopIsDWORD(CtBehavior method, int index)
            throws BadBytecode {

        var analyzer = new Analyzer();
        var frames = analyzer.analyze(
                method.getDeclaringClass(),
                method.getMethodInfo()
        );

        var frame = frames[index];

        // fetch type of the top element
        var top = frame.peek();

        return top == Type.DOUBLE || top == Type.LONG;
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
     * @param method The current method
     * @param opcode The opcode of the current operation
     * @param i      The current instruction index
     * @param it     The code iterator
     * @return a positive value if the stack is increased, a negative value
     * if the stack is reduced; 0 if the stack remains invariant
     * @throws BadBytecode       if the current bytecode is flawed
     * @throws NotFoundException if a potential method type
     *                           cannot be identified
     */
    public static int getStackLevelChange(CtBehavior method,
                                          int opcode,
                                          int i,
                                          CodeIterator it)
            throws BadBytecode, NotFoundException {
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
        if (special.contains(opcode)) {
            final var constPool = it.get().getConstPool();
            switch (opcode) {
                case DUP2:
                case DUP2_X1:
                case DUP2_X2:
                    // those opcodes treat DWORD values differently
                    return stackTopIsDWORD(method, i) ? +1 : +2;
                case POP2:
                    // for DWORDs the stack is only decreased by 1
                    return stackTopIsDWORD(method, i) ? -1 : -2;
                case INVOKEINTERFACE:
                    var arg = it.s16bitAt(i + 1);
                    var desc = constPool.getInterfaceMethodrefType(arg);

                    // removes the object reference and returns a single value
                    // (or void) and reduces the stack by n (+1) values
                    // where n is the number of parameters
                    return isVoid(desc)
                            ? -(1 + Descriptor.numOfParameters(desc))
                            : -Descriptor.numOfParameters(desc);
                case INVOKESPECIAL:
                case INVOKEVIRTUAL:
                    arg = it.s16bitAt(i + 1);
                    desc = constPool.getMethodrefType(arg);

                    // removes the object reference and returns a single value
                    // (or void) and reduces the stack by n (+1) values
                    return isVoid(desc)
                            ? -(1 + Descriptor.numOfParameters(desc))
                            : -Descriptor.numOfParameters(desc);
                case INVOKEDYNAMIC:
                    arg = it.s16bitAt(i + 1);
                    desc = constPool.getInvokeDynamicType(arg);

                    // removes n values and puts the result on the stack
                    return isVoid(desc)
                            ? -Descriptor.numOfParameters(desc)
                            : 1 - Descriptor.numOfParameters(desc);
                case INVOKESTATIC:
                    arg = it.s16bitAt(i + 1);
                    desc = constPool.getMethodrefType(arg);

                    // removes n values and puts the result on the stack
                    return isVoid(desc)
                            ? -Descriptor.numOfParameters(desc)
                            : 1 - Descriptor.numOfParameters(desc);
                case MULTIANEWARRAY:
                    /*
                    1 byte for instruction
                    2 bytes for index argument
                    3rd byte is number of dimensions
                    (number of arguments to remove)

                    This count is subtracted from 1
                    which is the returned array reference
                    */
                    return 1 - it.byteAt(i + 3);
                default:
                    break;
            }
        }

        return 0;
    }

    /**
     * Calculates the new stack level based on the current level
     * and the given opcode that represents the next instruction.
     *
     * @param method     The current method
     * @param stackLevel The current stack level
     * @param opcode     The current operation
     * @param i          The current instruction index
     * @param it         The code iterator
     * @return the new stack level
     * @throws BadBytecode       if the current bytecode is flawed
     * @throws NotFoundException if a method type cannot be identified
     */
    public static int newStackLevel(CtBehavior method,
                                    int stackLevel,
                                    int opcode,
                                    int i,
                                    CodeIterator it)
            throws BadBytecode, NotFoundException {
        // if an instruction clears the stack, simply return 0
        if (clear.contains(opcode))
            return 0;

        return stackLevel + getStackLevelChange(method, opcode, i, it);
    }

    /**
     * Chechs if the given descriptor identifies a method without return value
     * (void).
     *
     * @param desc The method descriptor
     * @return {@code true} if the method does not return anything;
     * {@code false} otherwise
     * @throws NotFoundException if the return type cannot be found in the
     *                           class pool
     */
    public static boolean isVoid(String desc) throws NotFoundException {
        var returnType = Descriptor.getReturnType(desc, ClassPool.getDefault());

        return returnType == CtClass.voidType;
    }
}
