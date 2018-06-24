package at.jku.ssw.java.bytecode.reducer.runtypes;

import at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist;
import javassist.CtClass;

/**
 * Runtype that provides helper that are based on the Javassist library
 */
public interface JavassistHelper extends BytecodeTransformer<CtClass> {
    @Override
    default CtClass classFrom(byte[] bytecode) throws Exception {
        return Javassist.loadClass(bytecode);
    }

    @Override
    default byte[] bytecodeFrom(CtClass clazz) throws Exception {
        return Javassist.bytecode(clazz);
    }
}
