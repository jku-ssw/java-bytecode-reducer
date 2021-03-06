package at.jku.ssw.java.bytecode.reducer.io.cli;

import at.jku.ssw.java.bytecode.reducer.context.ContextFactory;
import at.jku.ssw.java.bytecode.reducer.context.ModuleRegistry;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.io.IoBuilder;

import java.util.Arrays;
import java.util.Optional;

public class CLIParser {
    private static final Logger logger = LogManager.getLogger();

    public ContextFactory parseArguments(String... args) throws ParseException {
        Options     options = generateArgumentOptions();
        CommandLine cmd     = generateCommandLine(options, args);

        // Command line already handled - exit
        if (cmd == null)
            return null;

        if (cmd.hasOption(CLIOptions.HELP)) {
            showHelp(options);
            return null;
        }

        if (cmd.hasOption(CLIOptions.VERSION)) {
            showVersion();
            return null;
        }

        if (cmd.hasOption(CLIOptions.LIST)) {
            showModules();
            return null;
        }

        if (cmd.hasOption(CLIOptions.VERBOSE))
            Configurator.setAllLevels(
                    LogManager.getRootLogger().getName(),
                    Level.DEBUG
            );
        else if (cmd.hasOption(CLIOptions.QUIET))
            Configurator.setAllLevels(
                    LogManager.getRootLogger().getName(),
                    Level.FATAL
            );

        String out        = getArg(cmd, CLIOptions.OUT);
        String tmp        = getArg(cmd, CLIOptions.TEMP);
        String workingDir = getArg(cmd, CLIOptions.WORKING_D);

        String[] filters = Optional
                .ofNullable(cmd.getOptionValues(CLIOptions.FILTER))
                .orElse(new String[0]);

        String[] fileArgs   = cmd.getArgs();
        String[] classFiles = fileArgs;

        String[] iTests = Optional
                .ofNullable(cmd.getOptionValues(CLIOptions.I_TESTS))
                .orElse(new String[0]);

        long timeout = Optional
                .ofNullable((Number) getArg(cmd, CLIOptions.TIMEOUT))
                .map(Number::longValue)
                .orElse(-1L);

        boolean keepTemp = cmd.hasOption(CLIOptions.KEEP_TEMP);

        // if no explicit tests are provided with the option,
        // the first file is assumed to be the test
        if (iTests.length == 0 && fileArgs.length > 1) {
            iTests = new String[]{fileArgs[0]};
            classFiles = Arrays.copyOfRange(fileArgs, 1, fileArgs.length);
        }

        return new ContextFactory(
                classFiles,
                iTests,
                workingDir,
                out,
                tmp,
                filters,
                keepTemp,
                timeout
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
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(
                IoBuilder.forLogger(logger).setLevel(Level.INFO).buildPrintWriter(),
                100,
                "jreduce",
                "",
                options,
                10,
                5,
                "",
                true
        );
    }

    private void showVersion() {
        final String version = "*0.1*";
        logger.info("Java Bytecode Reducer");
        logger.info("Version: {}", version);
    }

    private void showModules() {
        logger.info("The following reducer modules are available:");
        ModuleRegistry.allModules().stream()
                .map(Class::getSimpleName)
                .forEach(logger::info);
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

        Option iTest = Option.builder(CLIOptions.I_TESTS)
                .desc("The interestingness test file (test.{sh,bat} is assumed if no argument is supplied)")
                .longOpt("i-tests")
                .valueSeparator(',')
                .hasArgs()
                .required(false)
                .build();

        Option workingDir = Option.builder(CLIOptions.WORKING_D)
                .desc("The working directory in which the task is run (if omitted, the current directory is assumed)")
                .longOpt("working-dir")
                .hasArg(true)
                .required(false)
                .build();

        Option outDir = Option.builder(CLIOptions.OUT)
                .desc("The directory where results will be placed")
                .longOpt("out-dir")
                .hasArg(true)
                .required(false)
                .build();

        Option tempDir = Option.builder(CLIOptions.TEMP)
                .desc("The temporary directory where the intermediate test results will be placed")
                .longOpt("temp-dir")
                .hasArg(true)
                .required(false)
                .build();

        OptionGroup logging = new OptionGroup()
                .addOption(verbose)
                .addOption(quiet);
        logging.setRequired(false);

        Option timeout = Option.builder(CLIOptions.TIMEOUT)
                .desc("The timeout in seconds until runs of the test files are interrupted (to prevent infinite loops)")
                .longOpt("timeout")
                .hasArg(true)
                .required(false)
                .type(Number.class)
                .build();

        Option filter = Option.builder(CLIOptions.FILTER)
                .desc("Choose individual operations or modules to apply")
                .longOpt("filter")
                .valueSeparator(',')
                .hasArgs()
                .required(false)
                .build();

        options.addOption(CLIOptions.HELP, CLIOptions.HELP, false, "Display information about application usage")
                .addOption(CLIOptions.VERSION, CLIOptions.VERSION, false, "Print program version")
                .addOption(CLIOptions.KEEP_TEMP, "keep", false, "Keep temporary test directories and files")
                .addOption(CLIOptions.LIST, "list-modules", false, "List all available transformation modules")
                .addOption(workingDir)
                .addOption(outDir)
                .addOption(tempDir)
                .addOptionGroup(logging)
                .addOption(iTest)
                .addOption(timeout)
                .addOption(filter);

        return options;
    }
}
