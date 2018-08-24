package at.jku.ssw.java.bytecode.reducer.context;

import at.jku.ssw.java.bytecode.reducer.runtypes.Reducer;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

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

    /**
     * The available transformation modules in this run.
     */
    private final List<Class<? extends Reducer>> modules;

    Context(Path outDir,
            Path tempDir,
            List<Class<? extends Reducer>> modules,
            boolean keepTemp) {

        this.outDir = outDir;
        this.tempDir = tempDir;
        this.keepTemp = keepTemp;
        this.modules = modules;
    }

    public Stream<Class<? extends Reducer>> executionOrder() {
        return modules.stream();
    }
}
