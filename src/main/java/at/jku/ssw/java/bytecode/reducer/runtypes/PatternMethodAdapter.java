package at.jku.ssw.java.bytecode.reducer.runtypes;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM6;

public abstract class PatternMethodAdapter extends MethodVisitor {

    public PatternMethodAdapter(MethodVisitor methodVisitor) {
        super(ASM6, methodVisitor);
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
