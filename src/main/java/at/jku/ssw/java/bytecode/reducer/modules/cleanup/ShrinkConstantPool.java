package at.jku.ssw.java.bytecode.reducer.modules.cleanup;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.runtypes.JavassistHelper;
import at.jku.ssw.java.bytecode.reducer.runtypes.Reducer;

/**
 * Removes unused items from the constant pool.
 */
@Sound
public class ShrinkConstantPool implements Reducer, JavassistHelper {
    @Override
    public byte[] apply(byte[] bytecode) throws Exception {
        var clazz = classFrom(bytecode);

        clazz.getClassFile().compact();

        return bytecodeFrom(clazz);
    }
}
