package at.jku.ssw.java.bytecode.reducer.cli;

/**
 * Holds constants related to the CLI parameters.
 * TODO add options to keep temp files
 */
public interface CLIOptions {
    String HELP       = "help";
    String VERSION    = "version";
    String QUIET      = "q";
    String VERBOSE    = "v";
    String SEQUENTIAL = "s";
    String N_THREADS  = "n";
    String WORKING_D  = "d";
    String OUT        = "out";
    String TEMP       = "tmp";
    String I_TESTS    = "i";
}
