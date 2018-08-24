package at.jku.ssw.java.bytecode.reducer.modules.cleanup;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.runtypes.Reducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.ASM6;
import static org.objectweb.asm.Opcodes.NOP;

/**
 * Removes any NOPs that are still left in method instructions.
 */
@Sound
public class RemoveNOPs implements Reducer {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public byte[] apply(byte[] bytecode) {
        ClassWriter cw = new ClassWriter(0);
        ClassReader cr = new ClassReader(bytecode);

        AtomicInteger counter = new AtomicInteger(0);

        ClassVisitor cv = new ClassVisitor(ASM6, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                var visitor = super.visitMethod(access, name, descriptor, signature, exceptions);

                if (visitor == null)
                    return null;

                return new MethodVisitor(ASM6, visitor) {
                    @Override
                    public void visitInsn(int opcode) {
                        // ignore NOPs (don't write them)
                        if (opcode != NOP)
                            super.visitInsn(opcode);
                        else
                            counter.getAndIncrement();
                    }
                };
            }
        };

        cr.accept(cv, 0);

        logger.debug("Removed {} NOP instructions", counter.get());

        return cw.toByteArray();
    }
}
