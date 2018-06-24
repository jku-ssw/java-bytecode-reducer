package at.jku.ssw.java.bytecode.reducer.context;

import java.nio.file.Path;

/**
 * Stores properties of the current execution context.
 * Every information that the runner needs in order to work properly is
 * contained here.
 */
public class Context {

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

    Context(Path outDir,
            Path tempDir,
            boolean keepTemp) {

        this.outDir = outDir;
        this.tempDir = tempDir;
        this.keepTemp = keepTemp;
    }
}
