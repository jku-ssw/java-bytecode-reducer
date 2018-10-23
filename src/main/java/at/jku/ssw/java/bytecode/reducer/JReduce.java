package at.jku.ssw.java.bytecode.reducer;

import at.jku.ssw.java.bytecode.reducer.io.cli.CLIParser;
import at.jku.ssw.java.bytecode.reducer.context.ContextFactory;
import at.jku.ssw.java.bytecode.reducer.errors.DuplicateClassException;
import at.jku.ssw.java.bytecode.reducer.io.files.NamingStrategy;
import at.jku.ssw.java.bytecode.reducer.io.files.TempDir;
import at.jku.ssw.java.bytecode.reducer.utils.functional.Catch;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;

public class JReduce {
    private static final Logger logger = LogManager.getLogger();

    public static final int EXIT_SUCCESS        = 0;
    public static final int ERROR_INVALID_ARGS  = -1;
    public static final int INVALID_CLASS_FILES = -2;

    /**
     * Main application entry point.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        CLIParser cliParser = new CLIParser();

        try {
            ContextFactory contextFactory = cliParser.parseArguments(args);

            // no context received - exit
            if (contextFactory == null)
                System.exit(EXIT_SUCCESS);

            // initialize the context
            final var context = contextFactory.createContext();

            // retrieve the running order
            final var stages = context.executionOrder();

            // initialize the test suite
            final var testSuite = contextFactory.getTestSuite();

            // init the cache
            final var cache = contextFactory.initCache();

            // instantiate the temporary directory at the given location
            TempDir.at(context.tempDir).use(tempDir ->

                    // iterate all stages
                    stages.forEach(Catch.consumer(module -> {
                        final var reducer = module.getDeclaredConstructor().newInstance();

                        logger.info("Initializing reducer " + module.getSimpleName());

                        TempDir.at(NamingStrategy.ForInstance(reducer), tempDir).use(reducerDir ->
                                cache.classes().forEach(Catch.consumer(fileName -> {
                                    logger.info("Reducing file " + fileName);

                                    // write all other files to the temporary directory
                                    cache.write(reducerDir);

                                    var bytecode = cache.bytecode(fileName);

                                    /*
                                    This call applies the given reduction
                                    until the result is minimal.
                                    The result then is the last valid bytecode.
                                    */
                                    bytecode = reducer.apply(bytecode, result -> {
                                        /*
                                        this method is called for every
                                        intermediate result attempt,
                                        where "result" holds a potentially
                                        conflicting bytecode
                                        */

                                        var path = reducerDir.resolve(fileName);

                                        try {
                                            Files.write(path, result);
                                        } catch (IOException e) {
                                            logger.fatal(e);
                                        }

                                        // check bytecode validity
                                        var isValid = testSuite.test(reducerDir);

                                        if (isValid) {
                                            /*
                                            if the tests ran correctly, update
                                            the cached bytecode and write the
                                            intermediate result to the output
                                            directory
                                            */
                                            cache.update(fileName, result)
                                                    .write(context.outDir);
                                        }

                                        return isValid;
                                    });

                                    // place the (now valid) bytecode
                                    // in the cache
                                    cache.update(fileName, bytecode);
                                })), context.keepTemp);
                    })), context.keepTemp);

        } catch (ParseException e) {
            logger.fatal(e);
            System.exit(ERROR_INVALID_ARGS);
        } catch (IOException e) {
            logger.fatal("Could not initialize working directory", e);
        } catch (DuplicateClassException e) {
            logger.fatal(e);
            System.exit(INVALID_CLASS_FILES);
        }
    }

}
