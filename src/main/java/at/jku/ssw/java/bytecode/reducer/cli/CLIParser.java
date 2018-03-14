package at.jku.ssw.java.bytecode.reducer.cli;

import at.jku.ssw.java.bytecode.reducer.Context;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Arrays;
import java.util.Optional;

public class CLIParser {
    private static final Logger logger = LogManager.getLogger();

    public Context parseArguments(String[] args) throws ParseException {
        Options     options = generateArgumentOptions();
        CommandLine cmd     = generateCommandLine(options, args);

        // Command line already handled - exit
        if (cmd == null)
            return null;

        int nThreads = 0; // TODO set default

        if (cmd.hasOption(CLIOptions.VERBOSE))
            Configurator.setAllLevels(
                    LogManager.getRootLogger().getName(),
                    Level.DEBUG
            );
        else if (cmd.hasOption(CLIOptions.QUIET))
            Configurator.setAllLevels(
                    LogManager.getRootLogger().getName(),
                    Level.FATAL
            ); // TODO maybe change to 'OFF'?

        if (cmd.hasOption(CLIOptions.SEQUENTIAL))
            nThreads = 1;
        else if (cmd.hasOption(CLIOptions.N_THREADS))
            nThreads = Optional
                    .ofNullable((Number) getArg(cmd, CLIOptions.N_THREADS))
                    .map(Number::intValue)
                    .orElse(0);

        String out = getArg(cmd, CLIOptions.OUT);
        String tmp = getArg(cmd, CLIOptions.TEMP);

        // TODO set default values
        String[] iTests = Optional
                .ofNullable(cmd.getOptionValues(CLIOptions.I_TESTS))
                .orElse(new String[0]);

        String[] classFiles = cmd.getArgs();

        // TODO remove after testing
        System.out.println("nThreads = " + nThreads);
        System.out.println("out = " + out);
        System.out.println("tmp = " + tmp);
        System.out.println("classFiles = " + Arrays.toString(classFiles));
        System.out.println("iTests = " + Arrays.toString(iTests));

        return new Context(
                classFiles,
                iTests,
                out,
                tmp,
                nThreads
        );
    }

    @SuppressWarnings("unchecked")
    private <T> T getArg(CommandLine cmd, String option) throws ParseException {
        return (T) cmd.getParsedOptionValue(option);
    }

    private CommandLine generateCommandLine(Options options, String[] args)
            throws ParseException {
        CommandLineParser parser = new DefaultParser();

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase(CLIOptions.HELP)) {
                showHelp(options);
                return null;
            } else if (args[0].equalsIgnoreCase(CLIOptions.VERSION)) {
                showVersion();
                return null;
            }
        }

        return parser.parse(options, args);
    }

    private void showHelp(Options options) {
        // TODO print additional usage information
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("jreduce", options);
    }

    private void showVersion() {
        // TODO either set constant or emit by Gradle
        final String version = "*0.1*";
        logger.info("Java Bytecode Reducer");
        logger.info("Version: {}", version);
    }

    private Options generateArgumentOptions() {
        Options options = new Options();

        Option verbose = Option.builder(CLIOptions.VERBOSE)
                .desc("Verbose logging")
                .longOpt("verbose")
                .hasArg(false)
                .required(false)
                .build();

        Option quiet = Option.builder(CLIOptions.QUIET)
                .desc("Suppress log messages")
                .longOpt("quiet")
                .hasArg(false)
                .required(false)
                .build();

        Option sequential = Option.builder(CLIOptions.SEQUENTIAL)
                .desc("Run in a single thread")
                .longOpt("sequential")
                .hasArg(false)
                .required(false)
                .build();

        // TODO would this be useful?
//        Option time = Option.builder("t")
//                .desc("Limit execution time (maximum duration in seconds)")
//                .longOpt("time")
//                .hasArg(false)
//                .required(false)
//                .numberOfArgs(1)
//                .type(Long.class)
//                .build();

        Option nthreads = Option.builder(CLIOptions.N_THREADS)
                .desc("Maximum number of threads to operate concurrently")
                .longOpt("nthreads")
                .hasArg(true)
                .required(false)
                .type(Integer.class)
                .build();

        Option iTest = Option.builder(CLIOptions.I_TESTS)
                .desc("The interestingness test file (test.{sh,bat} is assumed if no argument is supplied)")
                .longOpt("itest")
                .valueSeparator(',')
                .hasArgs()
                .required(false)
                .build();

        OptionGroup logging = new OptionGroup()
                .addOption(verbose)
                .addOption(quiet);
        logging.setRequired(false);

        OptionGroup threading = new OptionGroup()
                .addOption(sequential)
                .addOption(nthreads);
        threading.setRequired(false);

        options.addOption(CLIOptions.HELP, "Display information about application usage")
                .addOption(CLIOptions.VERSION, "Print program version")
                .addOption(CLIOptions.OUT, true, "The directory where results will be placed")
                .addOption(CLIOptions.TEMP, "temp", true, "The temporary directory where the intermediate test results will be placed")
                .addOptionGroup(logging)
                .addOptionGroup(threading)
                .addOption(iTest);

        return options;
    }
}
