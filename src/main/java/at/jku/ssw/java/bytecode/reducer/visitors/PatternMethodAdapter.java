package at.jku.ssw.java.bytecode.reducer.visitors;

import org.objectweb.asm.*;

/**
 * A method adapter that allows to detect patterns and rollback on certain
 * mismatches. Is modelled after the `MethodVisitor` of same name provided
 * in the ASM guide.
 */
public abstract class PatternMethodAdapter extends MethodVisitor {

    /**
     * The method descriptor.
     */
    protected final String descriptor;

    /**
     * Creates a new `PatternMethodAdapter` with the given API version,
     * the given parent visitor for the given method.
     *
     * @param api           The ASM API version (cf. class file version)
     * @param methodVisitor The parent visitor
     * @param descriptor    The method descriptor
     */
    public PatternMethodAdapter(int api, MethodVisitor methodVisitor, String descriptor) {
        super(api, methodVisitor);
        this.descriptor = descriptor;
    }

    /**
     * The method that is called whenever an instruction is encountered
     * that does not match the pattern.
     */
    protected abstract void visitInsn();

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitInsn(int opcode) {
        visitInsn();
        mv.visitInsn(opcode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitIntInsn(int opcode, int operand) {
        visitInsn();
        mv.visitIntInsn(opcode, operand);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitVarInsn(int opcode, int var) {
        visitInsn();
        mv.visitVarInsn(opcode, var);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitTypeInsn(int opcode, String desc) {
        visitInsn();
        mv.visitTypeInsn(opcode, desc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        visitInsn();
        mv.visitFieldInsn(opcode, owner, name, desc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitJumpInsn(int opcode, Label label) {
        visitInsn();
        mv.visitJumpInsn(opcode, label);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitLdcInsn(Object cst) {
        visitInsn();
        mv.visitLdcInsn(cst);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitIincInsn(int var, int increment) {
        visitInsn();
        mv.visitIincInsn(var, increment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        visitInsn();
        mv.visitTableSwitchInsn(min, max, dflt, labels);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        visitInsn();
        mv.visitLookupSwitchInsn(dflt, keys, labels);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        visitInsn();
        mv.visitMultiANewArrayInsn(desc, dims);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        visitInsn();
        super.visitFrame(type, nLocal, local, nStack, stack);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitLabel(Label label) {
        visitInsn();
        super.visitLabel(label);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        visitInsn();
        super.visitMaxs(maxStack, maxLocals);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitParameter(String name, int access) {
        visitInsn();
        super.visitParameter(name, access);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        visitInsn();
        return super.visitAnnotationDefault();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        visitInsn();
        return super.visitAnnotation(descriptor, visible);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        visitInsn();
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        visitInsn();
        super.visitAnnotableParameterCount(parameterCount, visible);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        visitInsn();
        return super.visitParameterAnnotation(parameter, descriptor, visible);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitAttribute(Attribute attribute) {
        visitInsn();
        super.visitAttribute(attribute);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitCode() {
        visitInsn();
        super.visitCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        visitInsn();
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        visitInsn();
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        visitInsn();
        return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        visitInsn();
        super.visitTryCatchBlock(start, end, handler, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        visitInsn();
        return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        visitInsn();
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        visitInsn();
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitLineNumber(int line, Label start) {
        visitInsn();
        super.visitLineNumber(line, start);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitEnd() {
        visitInsn();
        super.visitEnd();
    }
}
