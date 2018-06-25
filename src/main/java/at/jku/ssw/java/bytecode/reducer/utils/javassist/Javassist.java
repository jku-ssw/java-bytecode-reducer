package at.jku.ssw.java.bytecode.reducer.utils.javassist;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.ClassFile;

import java.io.*;

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
     * Retrieves the bytecode of the given class.
     *
     * @param clazz The class whose bytecode is retrieved
     * @return the bytecode that represents the given class
     * @throws IOException if the bytecode cannot be generated
     */
    public static byte[] bytecode(CtClass clazz) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            clazz.getClassFile().write(new DataOutputStream(out));

            return out.toByteArray();
        }
    }

    /**
     * Retrieves the bytecode of the given class.
     *
     * @param clazz The class whose bytecode is retrieved
     * @return the bytecode that represents the given class
     * @throws IOException if the bytecode cannot be generated
     */
    public static byte[] bytecode(ClassFile clazz) throws IOException {
        var ba = new ByteArrayOutputStream();

        clazz.write(new DataOutputStream(ba));

        return ba.toByteArray();
    }

}
