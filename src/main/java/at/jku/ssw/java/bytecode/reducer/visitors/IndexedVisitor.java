package at.jku.ssw.java.bytecode.reducer.visitors;

import org.objectweb.asm.*;

/**
 * {@link MethodVisitor} extension that keeps track of the current instruction
 * count. Note that this count does not actually reflect the bytecode indices
 * but helps to remember certain positions for subsequent visitor invocations.
 */
public abstract class IndexedVisitor extends PatternMethodAdapter {

    private int index;

    public IndexedVisitor(int api, MethodVisitor methodVisitor, String descriptor) {
        super(api, methodVisitor, descriptor);
        this.index = 0;
    }

    protected final int index() {
        return index;
    }

    protected final void next() {
        index++;
    }

    @Override
    public void visitParameter(String name, int access) {
        super.visitParameter(name, access);
        next();
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        AnnotationVisitor v = super.visitAnnotationDefault();
        next();
        return v;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AnnotationVisitor v = super.visitAnnotation(descriptor, visible);
        next();
        return v;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        AnnotationVisitor v = super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
        next();
        return v;
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        super.visitAnnotableParameterCount(parameterCount, visible);
        next();
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        AnnotationVisitor v = super.visitParameterAnnotation(parameter, descriptor, visible);
        next();
        return v;
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        super.visitAttribute(attribute);
        next();
    }

    @Override
    public void visitCode() {
        super.visitCode();
        next();
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        super.visitFrame(type, nLocal, local, nStack, stack);
        next();
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
        next();
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);
        next();
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
        next();
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        super.visitTypeInsn(opcode, type);
        next();
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        super.visitFieldInsn(opcode, owner, name, descriptor);
        next();
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        next();
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        next();
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
        next();
    }

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
        next();
    }

    @Override
    public void visitLdcInsn(Object value) {
        super.visitLdcInsn(value);
        next();
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        super.visitIincInsn(var, increment);
        next();
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        super.visitTableSwitchInsn(min, max, dflt, labels);
        next();
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        super.visitLookupSwitchInsn(dflt, keys, labels);
        next();
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
        next();
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        AnnotationVisitor v = super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
        next();
        return v;
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        super.visitTryCatchBlock(start, end, handler, type);
        next();
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        AnnotationVisitor v = super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
        next();
        return v;
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
        next();
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        AnnotationVisitor v = super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
        next();
        return v;
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
        next();
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, maxLocals);
        next();
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        next();
    }
}
