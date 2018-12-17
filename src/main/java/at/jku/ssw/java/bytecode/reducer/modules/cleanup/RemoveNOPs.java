package at.jku.ssw.java.bytecode.reducer.modules.cleanup;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.runtypes.ForcibleReducer;
import at.jku.ssw.java.bytecode.reducer.states.State;
import at.jku.ssw.java.bytecode.reducer.utils.cachetypes.CodePosition;
import at.jku.ssw.java.bytecode.reducer.visitors.IndexedVisitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.objectweb.asm.Opcodes.ASM6;
import static org.objectweb.asm.Opcodes.NOP;

/**
 * Removes any NOPs that are still left in method instructions.
 */
@Sound
public class RemoveNOPs implements ForcibleReducer<CodePosition> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public State.Experimental<CodePosition> apply(State.Stable<CodePosition> stable) {
        byte[] bytecode = stable.bytecode();

        ClassWriter cw = new ClassWriter(0);
        ClassReader cr = new ClassReader(bytecode);

        AtomicReference<CodePosition> candidate = new AtomicReference<>();

        ClassVisitor cv = new ClassVisitor(ASM6, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                var visitor = super.visitMethod(access, name, descriptor, signature, exceptions);

                if (visitor == null)
                    return null;

                return new IndexedVisitor(ASM6, visitor, name) {
                    @Override
                    protected void visitInsn() {
                    }

                    @Override
                    public void visitInsn(int opcode) {
                        var cp = new CodePosition(name, index());

                        if (opcode != NOP ||
                                candidate.get() != null ||
                                stable.isCached(cp))
                            // if it is not a NOP,
                            // or if there already was one found
                            // or if the current position is already cached,
                            // include the instruction
                            super.visitInsn(opcode);
                        else {
                            // otherwise ignore the NOP
                            candidate.set(cp);
                            next();
                        }
                    }
                };
            }
        };

        cr.accept(cv, 0);

        return Optional.ofNullable(candidate.get())
                .map(cp -> {
                    logger.debug("{}: Remove NOP", cp);
                    return cp;
                })
                .map(cp -> stable.toResult(cw.toByteArray(), cp))
                .orElseGet(stable::toMinimalResult);
    }

}
