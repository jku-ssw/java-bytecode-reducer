package at.jku.ssw.java.bytecode.reducer.utils.javassist;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class that contains helpers for Javassist.
 */
public class Javassist {

    private Javassist() {
    }

    /**
     * Loads the class from the given bytes (that should contain a class file).
     *
     * @param bytecode A byte array that describes the bytecode of a class
     * @return the {@link CtClass} corresponding to the bytecode
     * @throws IOException if the bytecode is invalid
     */
    public static CtClass loadClass(byte[] bytecode) throws IOException {
        try (InputStream is = new ByteArrayInputStream(bytecode)) {
            return ClassPool.getDefault().makeClass(is);
        }
    }

    /**
     * Retrieves the bytecode of the given class without locking the instance.
     *
     * @param clazz The class whose bytecode is retrieved
     * @return the bytecode that represents the given class
     * @throws IOException            if the bytecode cannot be generated
     * @throws CannotCompileException if the class does not compile
     */
    public static byte[] bytecode(CtClass clazz)
            throws IOException, CannotCompileException {
        byte[] bytecode = clazz.toBytecode();
        clazz.defrost();
        return bytecode;
    }

}
