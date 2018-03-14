package at.jku.ssw.java.bytecode.reducer;

import java.util.Arrays;
import java.util.List;

public class Context {

    // TODO set value
    public static final int    DEFAULT_THREAD_NUM = 5;
    public static final String DEFAULT_OUT        = "out";
    public static final String DEFAULT_TEMP       = "tmp";

    /**
     * The class files that should be reduced.
     */
    public final List<String> classFiles;

    /**
     * The names of the individual test scripts that have to be passed
     * in order for a reduction result to be "interesting"-
     */
    public final List<String> iTests;

    /**
     * Directory to write the output files.
     */
    public final String outDir;

    /**
     * Path to the temporary directory to copy the intermediate results.
     */
    public final String tempDir;

    /**
     * The maximum number of threads to generate for the tests.
     */
    public final int nThreads;

    public Context(String[] classFiles,
                   String[] iTests,
                   String outDir,
                   String tempDir,
                   int nThreads) {

        this.classFiles = Arrays.asList(classFiles);
        this.iTests = Arrays.asList(iTests);
        this.outDir = outDir == null ? DEFAULT_OUT : outDir;
        this.tempDir = tempDir == null ? DEFAULT_TEMP : tempDir;
        this.nThreads = nThreads == 0 ? DEFAULT_THREAD_NUM : nThreads;
    }
}
