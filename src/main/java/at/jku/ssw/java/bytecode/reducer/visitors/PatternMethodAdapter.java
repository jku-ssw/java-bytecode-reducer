package at.jku.ssw.java.bytecode.reducer.visitors;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public abstract class PatternMethodAdapter extends MethodVisitor {

    protected final String descriptor;

    public PatternMethodAdapter(int api, MethodVisitor methodVisitor, String descriptor) {
        super(api, methodVisitor);
        this.descriptor = descriptor;
    }

    protected abstract void visitInsn();

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        visitInsn();
        super.visitFrame(type, nLocal, local, nStack, stack);
    }

    @Override
    public void visitLabel(Label label) {
        visitInsn();
        super.visitLabel(label);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        visitInsn();
        super.visitMaxs(maxStack, maxLocals);
    }
}
