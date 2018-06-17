package at.jku.ssw.java.bytecode.reducer.context;

import at.jku.ssw.java.bytecode.reducer.utils.functional.TFunction;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Caches the analyzed classes and their corresponding current bytecode.
 */
public class Cache {

    /**
     * Maps the file names to their current bytecode.
     */
    private final Map<Path, byte[]> bytecodes;

    /**
     * Initialize a cache that stores the bytecodes of the given files.
     *
     * @param files The files to cache the bytecodes for
     * @return a new cache instance
     */
    public static Cache of(List<Path> files) {
        return new Cache(
                files.stream().collect(Collectors.toMap(
                        Path::getFileName,
                        (TFunction<Path, byte[]>) Files::readAllBytes)
                )
        );
    }

    /**
     * Creates a new cache based on the given mapping.
     *
     * @param bytecodes A mapping from files to their bytecodes
     */
    private Cache(Map<Path, byte[]> bytecodes) {
        this.bytecodes = bytecodes;
    }

    /**
     * Updates the bytecode for the given file.
     *
     * @param file        The file who's bytecode was modified
     * @param newBytecode The new bytecode
     * @return the updated cache instance
     */
    public Cache update(Path file, byte[] newBytecode) {
        bytecodes.put(file, newBytecode);

        return this;
    }

    /**
     * Retrieves the bytecode of the given file.
     *
     * @param file The file who's bytecode is cached
     * @return the bytecode of the corresponding file or null if the file
     * was not cached
     */
    public byte[] bytecode(Path file) {
        var bytecode = bytecodes.get(file);

        return Arrays.copyOf(bytecode, bytecode.length);
    }
}
