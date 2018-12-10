package at.jku.ssw.java.bytecode.reducer.modules.remove.local.self.assignments;

import at.jku.ssw.java.bytecode.reducer.visitors.PatternMethodAdapter;
import org.objectweb.asm.MethodVisitor;

import static at.jku.ssw.java.bytecode.reducer.modules.remove.local.self.assignments.MethodAdapter.State.INIT;
import static at.jku.ssw.java.bytecode.reducer.modules.remove.local.self.assignments.MethodAdapter.State.LOADED;
import static at.jku.ssw.java.bytecode.reducer.modules.remove.local.self.assignments.MethodAdapter.Type.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * Matches instruction sequences that load the value of a local variable and
 * then reassign it to the same variable (i.e. no effect).
 */
class MethodAdapter extends PatternMethodAdapter {

    /**
     * No instruction.
     */
    private static final int NONE = -1;

    /* Matching states */
    enum State {
        INIT, LOADED
    }

    /* Possible type descriptors of the load / store instructions */
    enum Type {
        A, D, F, I, L
    }

    /**
     * Current state in the matching process.
     */
    private State state = INIT;

    /**
     * The index of the loaded variable (e.g. 0 for `xLOAD_0`).
     */
    private int i = NONE;

    /**
     * The instruction (`xLOAD_y`) that initiated the potential self
     * assignment.
     */
    private int ins = NONE;

    /**
     * The type of the matched load instruction.
     */
    private Type t = null;

    MethodAdapter(MethodVisitor methodVisitor, String descriptor) {
        super(ASM6, methodVisitor, descriptor);
    }

    private void matchLoad(int opcode, int var, Type type) {
        // the pattern is triggered when a `xLOAD_y` instruction
        // is encountered
        if (state == LOADED) {
            // if another `xLOAD_y` instruction is detected,
            // the matching stalls
            accept();
        }

        state = LOADED;
        ins = opcode;
        i = var;
        t = type;
    }

    private void matchStore(int opcode, int var, Type type) {
        // matching fails if no `xLOAD_y` instruction preempted this
        // `xSTORE_y` instruction
        if (state == INIT) {
            visitInsn();
            mv.visitVarInsn(opcode, var);
            return;
        }

        // matching fails if the indices do not match
        if (i != var) {
            visitInsn();
            mv.visitVarInsn(opcode, var);
            return;
        }

        // matching fails if the types do not match
        if (t != type) {
            visitInsn();
            mv.visitVarInsn(opcode, var);
            return;
        }

        // pattern detected
        state = INIT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitVarInsn(int opcode, int var) {
        switch (opcode) {
            case ALOAD:
                matchLoad(opcode, var, A);
                break;
            case DLOAD:
                matchLoad(opcode, var, D);
                break;
            case FLOAD:
                matchLoad(opcode, var, F);
                break;
            case ILOAD:
                matchLoad(opcode, var, I);
                break;
            case LLOAD:
                matchLoad(opcode, var, L);
                break;
            case ASTORE:
                matchStore(opcode, var, A);
                break;
            case DSTORE:
                matchStore(opcode, var, D);
                break;
            case FSTORE:
                matchStore(opcode, var, F);
                break;
            case ISTORE:
                matchStore(opcode, var, I);
                break;
            case LSTORE:
                matchStore(opcode, var, L);
                break;
            default:
                visitInsn();
                mv.visitVarInsn(opcode, var);
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void visitInsn() {
        // accept any skipped instruction up to now
        if (state == LOADED) {
            accept();
        }

        state = INIT;
        i = NONE;
        ins = NONE;
    }

    /**
     * Writes the matched instruction.
     */
    private void accept() {
        mv.visitVarInsn(ins, i);
    }
}
