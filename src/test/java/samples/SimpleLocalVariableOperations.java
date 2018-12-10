package samples;

import org.objectweb.asm.ClassWriter;

import static org.objectweb.asm.Opcodes.*;

public class SimpleLocalVariableOperations extends BytecodeSample {

    public static Bytecode bytecode(int version) {
        var instance = new SimpleLocalVariableOperations();
        return instance.new Bytecode(
                instance.multipleAssignments(
                        instance.assemble(version)
                ).toByteArray()
        );
    }

    /**
     * <code>
     * ACC_PUBLIC multipleAssignments()V:
     * ICONST_0
     * ISTORE_1
     * ILOAD_1
     * ISTORE_2
     * ILOAD_2
     * ISTORE_2
     * ICONST_2
     * ISTORE_3
     * LDC 99
     * ISTORE_4
     * ILOAD_3
     * ILOAD_4
     * ISTORE_4
     * ISTORE_4
     * ALOAD_0
     * ASTORE_0
     * </code>
     *
     * @param cw The class writer
     * @return the same class writer
     */
    public ClassWriter multipleAssignments(ClassWriter cw) {
        var mv = cw.visitMethod(ACC_PUBLIC, "multipleAssignments", "()V", null, null);
        mv.visitCode();

        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitVarInsn(ISTORE, 2);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitVarInsn(ISTORE, 2);
        mv.visitInsn(ICONST_2);
        mv.visitVarInsn(ISTORE, 3);
        mv.visitLdcInsn(99);
        mv.visitVarInsn(ISTORE, 4);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitVarInsn(ISTORE, 4);
        mv.visitVarInsn(ISTORE, 4);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ASTORE, 0);

        mv.visitMaxs(2, 5);
        mv.visitEnd();
        return cw;
    }

    public static ClassWriter clazz(int version) {
        return new SimpleLocalVariableOperations().assemble(version);
    }
}
