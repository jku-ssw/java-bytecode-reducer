package at.jku.ssw.java.bytecode.reducer.visitors;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

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
}
