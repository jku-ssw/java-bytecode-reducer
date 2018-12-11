package samples;

import org.objectweb.asm.ClassWriter;

import static org.objectweb.asm.Opcodes.*;

public class SimpleFieldOperations extends BytecodeSample {

    public static Bytecode bytecode(int version) {
        var instance = new SimpleFieldOperations();
        return instance.new Bytecode(
                instance.assignSelfToFieldDup(
                        instance.assign10ToField(
                                instance.assignSelfToField(
                                        instance.assemble(version)
                                )
                        )
                ).toByteArray()
        );
    }

    /**
     * <code>
     * ACC_PUBLIC assignSelfToField()V:
     * ALOAD_0
     * ALOAD_0
     * GETFIELD #foo
     * PUTFIELD #foo
     * </code>
     *
     * @param cw The class writer
     * @return the same class writer
     */
    public ClassWriter assignSelfToField(ClassWriter cw) {
        var mv = cw.visitMethod(ACC_PUBLIC, "assignSelfToField", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, internalName, "foo", "I");
        mv.visitFieldInsn(PUTFIELD, internalName, "foo", "I");
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();
        return cw;
    }

    /**
     * <code>
     * ACC_PUBLIC assign10ToField()V:
     * ICONST 10
     * ALOAD_0
     * GETFIELD #foo
     * PUTFIELD #foo
     * </code>
     *
     * @param cw The class writer
     * @return the same class writer
     */
    public ClassWriter assign10ToField(ClassWriter cw) {
        var mv = cw.visitMethod(ACC_PUBLIC, "assign10ToField", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(10);
        mv.visitFieldInsn(PUTFIELD, internalName, "foo", "I");
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();
        return cw;
    }

    /**
     * <code>
     * ACC_PUBLIC ()V:
     * ALOAD_0
     * DUP
     * GETFIELD #foo
     * PUTFIELD #foo
     * </code>
     *
     * @param cw The class writer
     * @return the same class writer
     */
    public ClassWriter assignSelfToFieldDup(ClassWriter cw) {
        var mv = cw.visitMethod(ACC_PUBLIC, "assignSelfToFieldDup", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, internalName, "foo", "I");
        mv.visitFieldInsn(PUTFIELD, internalName, "foo", "I");
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();
        return cw;
    }

    public static ClassWriter clazz(int version) {
        return new SimpleFieldOperations().assemble(version);
    }

    @Override
    public ClassWriter prepareFields(ClassWriter cw) {
        cw.visitField(ACC_PUBLIC, "foo", "I", null, null);
        return cw;
    }
}
