package at.jku.ssw.java.bytecode.reducer.context;

import at.jku.ssw.java.bytecode.reducer.errors.DuplicateClassException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Caches the analyzed classes and their corresponding current bytecode.
 */
public class BytecodeCache {

    private static final Logger logger = LogManager.getLogger();

    /**
     * Maps the file names to their current bytecode.
     */
    private final Map<String, byte[]> bytecodes;

    /**
     * Initialize a cache that stores the bytecodes of the given class files.
     *
     * @param files The files to cache the bytecodes for
     * @throws IOException             if the bytecode cannot be read
     * @throws DuplicateClassException if two class files have the same name
     */
    BytecodeCache(Collection<Path> files)
            throws IOException, DuplicateClassException {

        final var bytecodes = new HashMap<String, byte[]>();

        for (var file : files) {
            var className = file.getFileName().toString();

            if (bytecodes.containsKey(className))
                throw new DuplicateClassException(className);

            bytecodes.put(className, Files.readAllBytes(file));
        }

        this.bytecodes = bytecodes;
    }

    /**
     * Updates the bytecode for the given file.
     *
     * @param className   The file who's bytecode was modified
     * @param newBytecode The new bytecode
     * @return the updated cache instance
     */
    public final BytecodeCache update(String className, byte[] newBytecode) {
        bytecodes.put(className, newBytecode);

        return this;
    }

    /**
     * Writes the current bytecode to a corresponding file in the
     * given directory.
     *
     * @param dest The target directory
     * @return the current cache instance
     */
    public final BytecodeCache write(Path dest) {
        bytecodes.forEach((file, bytecode) -> {
            var path = dest.resolve(file);

            try {
                Files.write(path, bytecode);
            } catch (IOException e) {
                logger.fatal(e);
            }
        });

        return this;
    }

    /**
     * Retrieves the bytecode of the given file.
     *
     * @param className The file who's bytecode is cached
     * @return the bytecode of the corresponding file or null if the file
     * was not cached
     */
    public final byte[] bytecode(String className) {
        var bytecode = bytecodes.get(className);

        return Arrays.copyOf(bytecode, bytecode.length);
    }

    /**
     * Returns the currently stored classes.
     *
     * @return a set of the contained class names
     */
    public final Set<String> classes() {
        return bytecodes.keySet();
    }

}
