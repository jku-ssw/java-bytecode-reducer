package at.jku.ssw.java.bytecode.reducer.runtypes;

/**
 * Represents a reduction of a class file.
 */
public interface Reducer {
    /**
     * Reduces the class file that is contained in the given bytes.
     *
     * @param bytecode The byte array that contains the byte code
     * @return the reduced byte code
     * @throws Exception if the byte code cannot be parsed or is invalid
     */
    byte[] apply(byte[] bytecode) throws Exception;
}
