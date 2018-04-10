package at.jku.ssw.java.bytecode.reducer.io;

import at.jku.ssw.java.bytecode.reducer.utils.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
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
     * Package-protected constructor only to be
     * used by the {@link TempDirBuilder}.
     *
     * @param path The path for the temporary directory
     */
    protected TempDir(Path path) {
        this.path = path;
    }

    /**
     * Executes the given procedure in the created temporary directory
     * and then deletes it.
     *
     * @param task The function to execute. Takes the temporary
     *             directory as a parameter
     * @return the temporary directory path (then deleted)
     * @throws IOException if the creation or clearing of the directory fails
     */
    public Path use(Consumer<Path> task) throws IOException {
        logger.debug("Creating temporary directory at {}", path);
        final Path path = Files.createDirectories(this.path);
        task.accept(path);
        logger.debug("Clearing temporary directory at {}", this.path);
        return FileUtils.delete(path);
    }
}
