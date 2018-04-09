package at.jku.ssw.java.bytecode.reducer.io;

import at.jku.ssw.java.bytecode.reducer.context.Context;
import at.jku.ssw.java.bytecode.reducer.utils.FileUtils;
import at.jku.ssw.java.bytecode.reducer.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * This class takes care of the file handling for a single test instance.
 * This includes the initiation of the test files - copying the class files,
 * interestingness tests - as well as the clean up afterwards - purging the
 * test directory or copying the new intermediate solution to the
 * output directory.
 * The structure of the test directory itself is as follows
 * (all test directories are contained in the
 * {@link at.jku.ssw.java.bytecode.reducer.context.ContextFactory#DEFAULT_TEMP} folder):
 * <ul>
 * <li>&#123;test-dir-name&#125;
 * <ul>
 * <li>&#123;class-file-1&#125;</li>
 * <li>&#123;class-file-2&#125;</li>
 * <li>&#123;...&#125;</li>
 * <li>&#123;class-file-n&#125;</li>
 * <li>&#123;interestingness-test-1&#125;</li>
 * <li>&#123;interestingness-test-2&#125;</li>
 * <li>&#123;...&#125;</li>
 * <li>&#123;interestingness-test-n&#125;</li>
 * </ul>
 * </li>
 * </ul>
 */
public class TestDirectory {
    //-------------------------------------------------------------------------
    // region Constants

    private static final Logger logger = LogManager.getLogger();

    // endregion
    //-------------------------------------------------------------------------
    // region Properties

    /**
     * The current test context. Holds properties like the different paths.
     */
    private final Context context;

    /**
     * This directory's path.
     */
    private Path path;

    /**
     * Flag that indicates that the directory has already been cleared.
     */
    private boolean purged;

    // endregion
    //-------------------------------------------------------------------------
    // region Constructor and overridden methods

    public TestDirectory(Context context, String name) throws IOException {
        assert context != null : "Cannot initialize test directory without context";
        assert StringUtils.isNotBlank(name) : "Cannot initialize nameless test directory";

        this.context = context;

        this.purged = false;

        // Set the current test directory
        this.path = Files.createDirectories(context.tempDir.resolve(name));

        // Copy the source files
        copy(context.classFiles, this.path);
        copy(context.iTests, this.path);
    }

    @Override
    public String toString() {
        return path.toString();
    }

// endregion
    //-------------------------------------------------------------------------
    // region Directory management methods

    public boolean lift() throws IOException {
        if (purged) return false;

        final Path out = context.outDir;

        // Copy the files into the output directory
        Files.list(path)
                .forEach(p -> {
                    try {
                        Files.copy(p, out.resolve(p.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        logger.fatal("Could not copy result file {} to output directory {}. The current results lie in {}: {}", p, out, path, e.getMessage());
                    }
                });

        return true;
    }

    public boolean clear() {
        if (purged) return true;

        try {
            FileUtils.delete(path);
            return purged = true;
        } catch (IOException e) {
            logger.fatal("Could not clear test directory {}: {}", path, e.getMessage());
        }

        return false;
    }

    // endregion
    //-------------------------------------------------------------------------
    // region IO utilities

    private void copy(List<Path> src, Path dst) {
        src.forEach(p -> {
            try {
                Files.copy(p, dst.resolve(p.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.fatal("Could not copy file {} to test directory {}: {}", p, dst, e.getMessage());
            }
        });
    }

    // endregion
    //-------------------------------------------------------------------------
}
