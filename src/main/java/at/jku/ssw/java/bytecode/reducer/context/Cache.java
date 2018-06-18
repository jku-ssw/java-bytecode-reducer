package at.jku.ssw.java.bytecode.reducer.context;

import at.jku.ssw.java.bytecode.reducer.errors.DuplicateClassException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Caches the analyzed classes and their corresponding current bytecode.
 */
public class Cache {

    /**
     * Maps the file names to their current bytecode.
     */
    private final Map<String, byte[]> bytecodes;

    /**
     * Initialize a cache that stores the bytecodes of the given class files.
     *
     * @param files The files to cache the bytecodes for
     * @return a new cache instance
     * @throws IOException             if the bytecode cannot be read
     * @throws DuplicateClassException if two class files have the same name
     */
    public static Cache of(Collection<Path> files)
            throws IOException, DuplicateClassException {

        final var bytecodes = new HashMap<String, byte[]>();

        for (var file : files) {
            var className = file.getFileName().toString();

            var duplicateFile = bytecodes.get(className);
            if (duplicateFile != null)
                throw new DuplicateClassException(className);

            bytecodes.put(className, Files.readAllBytes(file));
        }

        return new Cache(bytecodes);
    }

    /**
     * Creates a new cache based on the given mapping.
     *
     * @param bytecodes A mapping from files to their bytecodes
     */
    private Cache(Map<String, byte[]> bytecodes) {
        this.bytecodes = bytecodes;
    }

    /**
     * Updates the bytecode for the given file.
     *
     * @param className   The file who's bytecode was modified
     * @param newBytecode The new bytecode
     * @return the updated cache instance
     */
    public Cache update(String className, byte[] newBytecode) {
        bytecodes.put(className, newBytecode);

        return this;
    }

    /**
     * Retrieves the bytecode of the given file.
     *
     * @param className The file who's bytecode is cached
     * @return the bytecode of the corresponding file or null if the file
     * was not cached
     */
    public byte[] bytecode(String className) {
        var bytecode = bytecodes.get(className);

        return Arrays.copyOf(bytecode, bytecode.length);
    }

    /**
     * Returns the currently stored classes.
     *
     * @return a set of the contained class names
     */
    public Set<String> classes() {
        return bytecodes.keySet();
    }

}
