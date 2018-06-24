package at.jku.ssw.java.bytecode.reducer.runtypes;

/**
 * Helper that provides methods to retrieve meta type instances
 * from and convert them to bytecode.
 *
 * @param <T> The type that represents a class type
 */
public interface BytecodeTransformer<T> {
    /**
     * Loads the given bytecode as an instance of the class type.
     *
     * @param bytecode The bytecode that describes the class to reduce
     * @return a new instance of the class type representing the bytecode
     * @throws Exception if the bytecode is invalid or the object
     *                   cannot be instantiated
     */
    T classFrom(byte[] bytecode) throws Exception;

    /**
     * Retrieves the bytecode from the given instance of the class type.
     *
     * @param clazz The instance of the class type
     * @return the bytecode describing the class
     * @throws Exception if the bytecode cannot be extracted
     */
    byte[] bytecodeFrom(T clazz) throws Exception;
}
