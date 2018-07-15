package at.jku.ssw.java.bytecode.reducer.io;

import at.jku.ssw.java.bytecode.reducer.utils.OSUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Class that handles batch and shell scripts and enables those file types'
 * execution.
 */
public class ScriptRunner {

    private static Logger logger = LogManager.getLogger();

    public static final int EXIT_SUCCESS = 0;

    public static final int EXIT_TIMEOUT = 9;

    private final long timeout;

    public ScriptRunner(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Initializes a process to execute the script that is located at the given
     * path. This file must either be a batch (.bat) file (Windows)
     * or a shell (.sh) script in order to be executable by this utility.
     *
     * @param script The script to execute
     * @return the created process
     * @throws IOException if the process initiation fails
     */
    public Process exec(Path script) throws IOException {
        return new ProcessBuilder()
                .command(getCommand(script.getFileName().toString()))
                .directory(script.getParent().toFile())
                .inheritIO()
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start();
    }

    /**
     * Runs the file at the given path and blocks until
     * the process is complete.
     *
     * @param script The script to execute
     * @return the exit code of the process
     * @throws IOException          if the process initiation fails
     * @throws InterruptedException if the thread is interrupted while
     *                              waiting for the result
     * @see ScriptRunner#exec(Path)
     */
    public int execBlocking(Path script)
            throws IOException, InterruptedException {

        var process = exec(script);

        if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
            logger.warn("Execution of test {} took longer than {} seconds - it will be forcefully interrupted. Please provide your test files with a timeout to prevent infinite loops.", script, timeout);
            // destroy process
            process.destroy();
            // and wait for shutdown
            process.waitFor();
            return EXIT_TIMEOUT;
        }

        return process.exitValue();
    }

    /**
     * Returns the command and argument list to run the given file.
     * Here the appropriate command for the current
     * operating system is returned.
     *
     * @param filename The name of the executable file
     * @return the commands and options / arguments
     */
    private List<String> getCommand(String filename) {
        if (OSUtils.isWindows())
            return List.of("cmd.exe", "/c", filename);
        return List.of("sh", "-c", "./" + filename);
    }
}
