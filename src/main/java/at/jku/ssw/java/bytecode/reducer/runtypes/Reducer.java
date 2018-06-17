package at.jku.ssw.java.bytecode.reducer.runtypes;

import java.util.function.Predicate;

/**
 * Represents a reduction of a class file.
 */
@FunctionalInterface
public interface Reducer {

    /**
     * Reduces the class file that is contained in the given bytes.
     *
     * @param bytecode The byte array that contains the bytecode
     * @return the reduced bytecode
     * @throws Exception if the bytecode cannot be parsed or is invalid
     */
    byte[] apply(byte[] bytecode) throws Exception;

    /**
     * Reduces the given bytecode and tests its validity based on the given
     * {@link Predicate}.
     *
     * @param bytecode The byte array that contains the bytecode
     * @param test     Function that verifies whether the reduced bytecode is valid
     * @return the reduced bytecode or the unchanged original bytecode
     * if the test failed
     * @throws Exception if the bytecode cannot be parsed or is invalid
     */
    default byte[] apply(byte[] bytecode, Predicate<byte[]> test) throws Exception {
        var result = apply(bytecode);

        return test.test(result) ? result : bytecode;
    }
}
