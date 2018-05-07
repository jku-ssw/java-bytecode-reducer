package at.jku.ssw.java.bytecode.reducer.context;

import at.jku.ssw.java.bytecode.reducer.utils.OSUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Factory that is initialized with properties and file paths
 * in order to generate a valid test context.
 * TODO refactor
 */
public class ContextFactory {
    //-------------------------------------------------------------------------
    // region Constants

    private static final Logger logger = LogManager.getLogger();

    /**
     * Number of threads that are usually run concurrently in order to
     * process the given input files with different reducer modules.
     * TODO reasonable default value
     */
    public static final int DEFAULT_THREAD_NUM = 5;

    /**
     * The default output directory for the intermediate
     * and final result files.
     */
    public static final String DEFAULT_OUT = "out";

    /**
     * The default place to put the test's working directories.
     */
    public static final String DEFAULT_TEMP = ".tmp";
    /**
     * The maximum number of threads to operate at the same time.
     * TODO set reasonable max
     */
    public static final int    MAX_THREADS  = 10;

    // endregion
    //-------------------------------------------------------------------------
    // region Properties

    /**
     * Either relative or absolute paths to the class files.
     */
    private final String[] classFiles;

    /**
     * Either relative or absolute paths to the interestingness tests.
     */
    private final String[] iTests;

    /**
     * Relative or absolute path to the working directory that is used
     * to store the temporary and output directory.
     * The current directory is used, if no argument was specified.
     */
    private final String workingDir;

    /**
     * Output directory for the final / intermediate result files.
     * If a relative path is specified, the output directory for the context
     * is assumed to be a subdirectory of the working directory.
     */
    private final String outDir;

    /**
     * Temporary directory that is used to store the sources and
     * test files for the individual test runs.
     * If the path is relative, the temporary directory for the context is
     * assumed to be a subdirectory of the working directory.
     */
    private final String tempDir;

    /**
     * Keep temporary test files and directories instead of deleting
     * them after each run.
     */
    private final boolean keepTemp;

    /**
     * Matcher for test scripts.
     */
    private final PathMatcher scriptMatcher;

    /**
     * Matcher for class files.
     */
    private final PathMatcher classMatcher;

    // endregion
    //-------------------------------------------------------------------------
    // region Initialization

    public ContextFactory(String[] classFiles,
                          String[] iTests,
                          String workingDir,
                          String outDir,
                          String tempDir,
                          boolean keepTemp) {

        this.classFiles = classFiles;
        this.iTests = iTests;
        this.workingDir = workingDir == null ? "" : workingDir;
        this.outDir = outDir == null ? DEFAULT_OUT : outDir;
        this.tempDir = tempDir == null ? DEFAULT_TEMP : tempDir;
        this.keepTemp = keepTemp;

        String scriptPattern = OSUtils.isWindows() ? "glob:*.bat" : "glob:*.sh";
        scriptMatcher = FileSystems.getDefault().getPathMatcher(scriptPattern);
        classMatcher = FileSystems.getDefault().getPathMatcher("glob:*.class");
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Overridden methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContextFactory that = (ContextFactory) o;
        return Arrays.equals(classFiles, that.classFiles) &&
                Arrays.equals(iTests, that.iTests) &&
                Objects.equals(workingDir, that.workingDir) &&
                Objects.equals(outDir, that.outDir) &&
                Objects.equals(tempDir, that.tempDir);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(workingDir, outDir, tempDir);
        result = 31 * result + Arrays.hashCode(classFiles);
        result = 31 * result + Arrays.hashCode(iTests);
        return result;
    }

    @Override
    public String toString() {
        return "ContextFactory{" +
                "classFiles=" + Arrays.toString(classFiles) +
                ", iTests=" + Arrays.toString(iTests) +
                ", workingDir='" + workingDir + '\'' +
                ", outDir='" + outDir + '\'' +
                ", tempDir='" + tempDir + '\'' +
                '}';
    }

// endregion
    //-------------------------------------------------------------------------
    // region Factory methods

    /**
     * Initialize a new context and set up the directories.
     *
     * @return the new context
     */
    public Context createContext() throws IOException {
        Path workingDir = Files.createDirectories(Paths.get(this.workingDir));
        Path outDir     = Files.createDirectories(workingDir.resolve(this.outDir));
        Path tempDir    = Files.createDirectories(workingDir.resolve(this.tempDir));

        List<Path> classFiles = validateAndCopy(
                workingDir,
                this.classFiles,
                outDir,
                classMatcher
        );

        List<Path> iTests = validateAndCopy(
                workingDir,
                this.iTests,
                outDir,
                scriptMatcher
        );

        return new Context(
                classFiles,
                iTests,
                workingDir,
                outDir,
                tempDir,
                keepTemp
        );
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Utility methods

    /**
     * Validates the given file names and copies them to the output directory.
     *
     * @param workingDir The working directory (default reference for relative paths)
     * @param files      The files to analyze
     * @param out        The target directory
     * @param matcher    The required file ending
     * @return the filtered and verified list of files
     * @throws IOException if the file handlers run into problems
     */
    private List<Path> validateAndCopy(Path workingDir,
                                       String[] files,
                                       Path out,
                                       PathMatcher matcher)
            throws IOException {

        final Stream<Path> paths;

        if (files.length == 0)
            paths = scanFiles(workingDir, matcher);
        else
            paths = resolve(workingDir, files, matcher);

        return copy(paths, out)
                .collect(Collectors.toList());
    }

    /**
     * Resolve a number of file names / paths against a given root directory.
     * The given paths can either be absolute or relative.
     * If the paths are absolute the root path is ignored.
     *
     * @param root    Reference directory for relative paths
     * @param paths   The path descriptors that should be resolved to valid paths
     * @param matcher The required file ending
     * @return a stream of (absolute) paths representing the given descriptors
     */
    private Stream<Path> resolve(Path root, String[] paths, PathMatcher matcher) {
        return Arrays.stream(paths)
                .map(Paths::get)
                .map(root::resolve)
                .filter(p -> {
                    if (Files.isDirectory(p)) {
                        logger.warn("Skipping {} - not a file.", p);
                        return false;
                    } else if (Files.notExists(p)) {
                        logger.warn("Skipping {} - file not found.", p);
                        return false;
                    } else if (!matcher.matches(p)) {
                        logger.warn(p.getFileName());
                        logger.warn("Skipping {} - file does not match the required extension (\"{}\").", p, matcher);
                        return false;
                    }

                    return true;
                });
    }

    /**
     * Returns all regular files in the given directory that end
     * with the given string.
     *
     * @param workingDir The directory to scan
     * @param matcher    String that all files have to end with
     * @return a stream of paths that point to the result files
     * @throws IOException if the directory is a file or inaccessible
     */
    private Stream<Path> scanFiles(Path workingDir, PathMatcher matcher) throws IOException {
        return Files.list(workingDir)
                .filter(Files::isRegularFile)
                .filter(matcher::matches);
    }

    /**
     * Copies the given files to the given target directory.
     *
     * @param src The source files that have to be copied
     * @param out The destination path
     * @return a stream containing the resulting file copies
     */
    private Stream<Path> copy(Stream<Path> src, Path out) {
        return src
                .map(p -> {
                    try {
                        return Files.copy(p, out.resolve(p.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        logger.fatal("Could not copy {} to output directory {}: {}", p, out, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull);
    }

    // endregion
    //-------------------------------------------------------------------------
}
