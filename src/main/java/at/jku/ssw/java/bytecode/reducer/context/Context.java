package at.jku.ssw.java.bytecode.reducer.context;

import at.jku.ssw.java.bytecode.reducer.errors.DuplicateClassException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * Stores properties of the current execution context.
 * Every information that the runner needs in order to work properly is
 * contained here.
 * The field {@link #cache} directly stores the current bytecodes of the
 * respective files.
 */
public class Context {

    /**
     * The individual test scripts that have to be passed
     * in order for a reduction result to be "interesting"-
     */
    public final Set<Path> iTests;

    /**
     * Directory to write the output files.
     */
    public final Path outDir;

    /**
     * Path to the temporary directory to copy the intermediate results.
     */
    public final Path tempDir;

    /**
     * Flag that indicates that temporary directories should not be removed.
     */
    public final boolean keepTemp;

    /**
     * Holds the current file to bytecode mapping
     */
    public final Cache cache;

    Context(Set<Path> classFiles,
            Set<Path> iTests,
            Path outDir,
            Path tempDir,
            boolean keepTemp)
            throws IOException, DuplicateClassException {

        this.iTests = iTests;
        this.outDir = outDir;
        this.tempDir = tempDir;
        this.keepTemp = keepTemp;
        this.cache = Cache.of(classFiles);
    }
}
