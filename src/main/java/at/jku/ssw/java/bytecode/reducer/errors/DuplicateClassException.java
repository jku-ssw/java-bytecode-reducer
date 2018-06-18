package at.jku.ssw.java.bytecode.reducer.errors;

/**
 * Exception that is thrown if two class files of the same name
 * should be reduced.
 */
public class DuplicateClassException extends Exception {
    public DuplicateClassException(String className) {
        super("Duplicate file " + className);
    }
}
