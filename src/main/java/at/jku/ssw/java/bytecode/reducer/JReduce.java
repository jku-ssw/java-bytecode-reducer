package at.jku.ssw.java.bytecode.reducer;

import at.jku.ssw.java.bytecode.reducer.cli.CLIParser;
import at.jku.ssw.java.bytecode.reducer.context.ContextFactory;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JReduce {
    private static final Logger logger = LogManager.getLogger();

    public static final int EXIT_SUCCESS       = 0;
    public static final int ERROR_INVALID_ARGS = -1;

    /**
     * Main application entry point.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        CLIParser cliParser = new CLIParser();

        try {
            ContextFactory contextFactory = cliParser.parseArguments(args);

            // No context received - exit
            if (contextFactory == null)
                System.exit(EXIT_SUCCESS);

        } catch (ParseException e) {
            logger.fatal(e.getMessage());
            System.exit(ERROR_INVALID_ARGS);
        }

        logger.debug("Initialized test context");

        // TODO
    }

}
