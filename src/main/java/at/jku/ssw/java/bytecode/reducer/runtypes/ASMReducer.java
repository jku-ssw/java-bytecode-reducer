package at.jku.ssw.java.bytecode.reducer.runtypes;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.util.stream.Stream;

public interface ASMReducer extends Reducer {
    @Override
    default byte[] apply(byte[] bytecode) {

        // do not use "optimized" ClassWriter invocation (with reference
        // to the ClassReader instance, since this is discouraged for
        // non-additive transformations
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassReader cr = new ClassReader(bytecode);

        visitors(cw).forEach(v -> cr.accept(v, 0));

        return cw.toByteArray();
    }

    Stream<ClassVisitor> visitors(ClassVisitor parent);
}
