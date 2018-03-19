package at.jku.ssw.java.bytecode.reducer.context;

import java.nio.file.Path;
import java.util.List;

public class Context {

    /**
     * The class files that should be reduced.
     */
    public final List<Path> classFiles;

    /**
     * The individual test scripts that have to be passed
     * in order for a reduction result to be "interesting"-
     */
    public final List<Path> iTests;

    /**
     * The directory in which the task is done.
     */
    public final Path workingDir;

    /**
     * Directory to write the output files.
     */
    public final Path outDir;

    /**
     * Path to the temporary directory to copy the intermediate results.
     */
    public final Path tempDir;

    /**
     * The maximum number of threads to generate for the tests.
     */
    public final int nThreads;

    Context(List<Path> classFiles,
            List<Path> iTests,
            Path workingDir,
            Path outDir,
            Path tempDir,
            int nThreads) {
        
        this.classFiles = classFiles;
        this.iTests = iTests;
        this.workingDir = workingDir;
        this.outDir = outDir;
        this.tempDir = tempDir;
        this.nThreads = nThreads;
    }
}