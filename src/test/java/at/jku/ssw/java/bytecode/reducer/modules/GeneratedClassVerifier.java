package at.jku.ssw.java.bytecode.reducer.modules;

import samples.BytecodeSample.Bytecode;

public class GeneratedClassVerifier extends ClassLoader {

    /**
     * Checks whether the bytecode describes a valid class.
     *
     * @param bytecode The bytecode containing the class
     * @return {@code true} if the given class can be loaded;
     * {@code false} otherwise
     */
    public boolean isValid(Bytecode bytecode) {

        try {
            defineClass(
                    bytecode.className,
                    bytecode.bytecode,
                    0,
                    bytecode.bytecode.length
            );
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

}
