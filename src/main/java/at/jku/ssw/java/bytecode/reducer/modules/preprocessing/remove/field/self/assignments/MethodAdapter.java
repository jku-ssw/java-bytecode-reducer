package at.jku.ssw.java.bytecode.reducer.modules.preprocessing.remove.field.self.assignments;

import at.jku.ssw.java.bytecode.reducer.visitors.PatternMethodAdapter;
import org.objectweb.asm.MethodVisitor;

import static at.jku.ssw.java.bytecode.reducer.modules.preprocessing.remove.field.self.assignments.MethodAdapter.State.INIT;
import static org.objectweb.asm.Opcodes.*;

class MethodAdapter extends PatternMethodAdapter {

    private static final int NONE = -1;

    /* Matching states */
    enum State {
        INIT, INSTANCE_LOADED, INSTANCE_RELOADED, FIELD_LOADED
    }

    /**
     * Current state in the matching process.
     */
    private State state = INIT;

    /**
     * The index of the loaded instance (e.g. 0 for `ALOAD_0`).
     */
    private int i = NONE;

    /**
     * The first instruction (`ALOAD_x`) that initiated the potential field access
     * or the first instruction of a consecutive series
     * (`ALOAD_x` or `DUP`).
     */
    private int loadInstruction = NONE;

    /**
     * The consecutive `ALOAD_x` or `DUP` instruction that
     * has to duplicate or reload the instance to enable the `PUTFIELD`
     * afterwards.
     */
    private int nextInstruction = NONE;

    private String fieldOwner;
    private String fieldName;
    private String fieldDesc;

    MethodAdapter(MethodVisitor methodVisitor, String descriptor) {
        super(ASM6, methodVisitor, descriptor);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        if (opcode == ALOAD || opcode == DUP) {
            switch (state) {
                case INIT:
                    // the pattern is only triggered when
                    // an `ALOAD_x` is recognized
                    if (opcode == ALOAD) {
                        loadInstruction = opcode;
                        nextInstruction = NONE;
                        i = var;
                        state = State.INSTANCE_LOADED;
                        return;
                    }
                    break;
                case INSTANCE_LOADED:
                    if (opcode == ALOAD && var != i) {
                        // if another `ALOAD_x` instruction is detected,
                        // but the indices do not match,
                        // the matching stalls
                        accept(loadInstruction);
                        loadInstruction = ALOAD;
                        nextInstruction = NONE;
                        i = var;
                    } else {
                        // if an `ALOAD_x` or `DUP` instruction is encountered,
                        // the sequence is continued

                        nextInstruction = opcode;

                        state = State.INSTANCE_RELOADED;
                    }
                    return;
                case INSTANCE_RELOADED:
                    if (opcode == ALOAD && var != i) {
                        // if another `ALOAD_x` instruction is detected,
                        // but the indices do not match,
                        // the matching goes a step back
                        accept(loadInstruction);
                        accept(nextInstruction);
                        loadInstruction = ALOAD;
                        nextInstruction = NONE;
                        state = State.INSTANCE_LOADED;
                        i = var;
                    } else {
                        // if another `ALOAD_x` or `DUP` instruction is found,
                        // the matching stalls

                        // already a sequence detected, the first
                        // instruction has to be overwritten
                        // and the old instruction is written to the
                        // class file
                        accept(loadInstruction);

                        loadInstruction = nextInstruction;
                        nextInstruction = opcode;
                    }
                    return;
            }
        }

        visitInsn();
        mv.visitVarInsn(opcode, var);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        switch (state) {
            case INSTANCE_RELOADED:
                if (opcode == GETFIELD) {
                    // field loading detected -> progress
                    state = State.FIELD_LOADED;
                    fieldOwner = owner;
                    fieldName = name;
                    fieldDesc = descriptor;
                    return;
                }
                break;
            case FIELD_LOADED:
                if (opcode == PUTFIELD && name.equalsIgnoreCase(fieldName)) {
                    // full sequence detected -> reset
                    state = INIT;
                    return;
                }
                break;
            default:
                break;
        }
        visitInsn();
        mv.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    protected void visitInsn() {
        // accept any skipped instruction up to now
        switch (state) {
            case INSTANCE_LOADED:
                accept(ALOAD);
                break;
            case INSTANCE_RELOADED:
                accept(loadInstruction);
                accept(nextInstruction);
                break;
            case FIELD_LOADED:
                accept(loadInstruction);
                accept(nextInstruction);
                accept(GETFIELD);
                break;
            default:
                break;
        }

        state = INIT;
        i = NONE;
        loadInstruction = NONE;
        nextInstruction = NONE;
    }

    private void accept(int opcode) {
        if (opcode == ALOAD) {
            mv.visitVarInsn(ALOAD, i);
        } else if (opcode == DUP) {
            mv.visitInsn(DUP);
        } else if (opcode == GETFIELD) {
            mv.visitFieldInsn(GETFIELD, fieldOwner, fieldName, fieldDesc);
        }
    }
}
