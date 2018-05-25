package at.jku.ssw.java.bytecode.reducer.io;

import at.jku.ssw.java.bytecode.reducer.utils.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Simplifies the creation of temporary directories at a specific location.
 * This is created as a normal directory that is deleted upon exiting
 * the given block.
 */
public class TempDir {

    /**
     * Logger instance.
     */
    private static final Logger logger = LogManager.getLogger();

    /**
     * The path at which the directory is created.
     */
    protected final Path path;

    /**
     * The naming strategy to apply.
     */
    protected final NamingStrategy strat;

    /**
     * Package-protected constructor only to be
     * used by the factory method.
     *
     * @param strategy The Naming strategy to use for the directory generation
     * @param path     The path for the temporary directory
     */
    protected TempDir(NamingStrategy strategy, Path path) {
        this.path = path;
        this.strat = strategy;
    }

    /**
     * Create a temp directory at the given location.
     *
     * @param path The directory location
     * @return a new temporary directory with its root at the given path
     * @throws IOException if the path denotes a file
     *                     or a not writable directory
     */
    public static TempDir at(Path path)
            throws IOException {
        return at(NamingStrategy.Static(path.getFileName().toString()), path);
    }

    /**
     * Create a temp directory at the given location.
     *
     * @param strategy The naming strategy to apply
     * @param path     The directory location
     * @return a new temporary directory with its root at the given path
     * @throws IOException if the path denotes a file or a
     *                     not writable directory
     */
    public static TempDir at(NamingStrategy strategy, Path path)
            throws IOException {

        if (strategy == null)
            throw new NullPointerException();

        if (Files.exists(path) && !Files.isDirectory(path))
            throw new NotDirectoryException(path.toString());

        if (Files.isDirectory(path) && !Files.isWritable(path))
            throw new AccessDeniedException(path.toString());

        return new TempDir(strategy, path);
    }

    /**
     * Executes the given procedure in the created temporary directory
     * and then deletes it (if specified).
     *
     * @param task The function to execute. Takes the temporary
     *             directory as a parameter
     * @param keep Should the created directory (and all its contents)
     *             be deleted?
     * @return the temporary directory path (then deleted)
     * @throws IOException if the creation or clearing of the directory fails
     */
    public Path use(Consumer<Path> task, boolean keep) throws IOException {
        return strat.stream()
                .limit(NamingStrategy.MAX_ATTEMPTS)
                .map(path::resolve)
                .filter(p -> {
                    try {
                        return !Files.exists(p) ||
                                Files.isDirectory(p) &&
                                        FileUtils.isEmpty(p) &&
                                        Files.isWritable(p);
                    } catch (IOException e) {
                        return false;
                    }
                })
                .findAny()
                .map(p -> {
                    try {
                        logger.debug("Creating temporary directory at {}", p);
                        final Path path = Files.createDirectories(p);
                        task.accept(p);
                        logger.debug("Clearing temporary directory at {}", p);
                        return keep ? path : FileUtils.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .orElseThrow(() ->
                        new IOException("Could not generate temporary directory at " + path)
                );
    }

    /**
     * @see TempDir#use(Consumer, boolean)
     */
    public Path use(Consumer<Path> action) throws IOException {
        return use(action, false);
    }
}
