package at.jku.ssw.java.bytecode.reducer.io;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A factory to create {@link TempDirBuilder}s and then {@link TempDir}
 * instances.
 * Takes a naming strategy in order to get the directory name.
 */
public final class TempDirBuilder {
    private final NamingStrategy namingStrategy;

    /**
     * Single constructor that requires a naming strategy.
     *
     * @param namingStrategy A name generator (required)
     */
    private TempDirBuilder(NamingStrategy namingStrategy) {
        this.namingStrategy = Objects.requireNonNull(namingStrategy);
    }

    /**
     * Creates a new builder instance with the given name generator
     *
     * @param namingStrategy The naming strategy to apply
     * @return a new {@link TempDirBuilder} instance that
     * enables {@link TempDir} generation
     */
    public static TempDirBuilder withNamingStrategy(NamingStrategy namingStrategy) {
        return new TempDirBuilder(namingStrategy);
    }

    /**
     * Creates a persistent directory as a sub directory of the given path.
     * The designated {@link NamingStrategy} is used to get the appropriate name.
     *
     * @param path The location at which the directory should be created.
     * @return
     * @throws FileAlreadyExistsException
     */
    public TempDir at(Path path)
            throws IOException {
        if (Files.isDirectory(path) && !Files.isWritable(path))
            throw new AccessDeniedException(path.toString());

        return namingStrategy.stream()
                .map(path::resolve)
                .filter(Files::notExists)
                .map(TempDir::new)
                .findFirst()
                .orElseThrow(() ->
                        new IOException("Could not generate temporary directory at " + path)
                );
    }

    private Path validate(Path path) throws FileAlreadyExistsException {
        if (Files.isRegularFile(path))
            throw new FileAlreadyExistsException("Path references an existing file.");

        if (Files.exists(path))
            throw new FileAlreadyExistsException("Path references an existing directory.");

        return path;
    }

}
