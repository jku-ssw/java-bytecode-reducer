package at.jku.ssw.java.bytecode.reducer.io;

import at.jku.ssw.java.bytecode.reducer.utils.OSUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ScriptRunner {

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
                .start();
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
