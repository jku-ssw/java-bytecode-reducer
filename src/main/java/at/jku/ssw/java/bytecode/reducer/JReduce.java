package at.jku.ssw.java.bytecode.reducer;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.cli.CLIParser;
import at.jku.ssw.java.bytecode.reducer.context.ContextFactory;
import at.jku.ssw.java.bytecode.reducer.errors.DuplicateClassException;
import at.jku.ssw.java.bytecode.reducer.io.NamingStrategy;
import at.jku.ssw.java.bytecode.reducer.io.TempDir;
import at.jku.ssw.java.bytecode.reducer.modules.fields.*;
import at.jku.ssw.java.bytecode.reducer.modules.methods.RemoveEmptyMethods;
import at.jku.ssw.java.bytecode.reducer.modules.methods.RemoveMethodAttributes;
import at.jku.ssw.java.bytecode.reducer.modules.methods.RemoveUnusedMethods;
import at.jku.ssw.java.bytecode.reducer.runtypes.Reducer;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TConsumer;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

            // TODO invoke by loading class path files or specify otherwise
            // all available reduction operations
            final var modules = List.of(
                    RemoveUnusedFields.class,
                    RemoveUnusedMethods.class,
                    RemoveWriteOnlyFields.class,
                    RemoveEmptyMethods.class,
                    RemoveReadOnlyFields.class,
                    RemoveStaticFieldAttributes.class,
                    RemoveAllFieldAttributes.class,
                    RemoveMethodAttributes.class
            );

            final var pre = modules.stream()
                    .filter(c -> c.isAnnotationPresent(Sound.class))
                    .collect(Collectors.toList());

            final var core = modules.stream()
                    .filter(c -> c.isAnnotationPresent(Unsound.class))
                    .collect(Collectors.toList());

            /*
            Running order: apply sound operations first,
            then try experimental ones that may remove significant portions
            of the code and then run sound ones again to reduce assets that
            may have been affected by the core transformations.
            */
            final var stages = List.of(pre, core, pre).stream()
                    .flatMap(Collection::stream);

            // initialize the context
            final var context = contextFactory.createContext();


            // initialize the test suite
            final var testSuite = contextFactory.getTestSuite();

            // init the cache
            final var cache = contextFactory.initCache();

            // instantiate the temporary directory at the given location
            TempDir.at(context.tempDir).use(tempDir ->

                    // iterate all stages
                    stages.forEach((TConsumer<Class<? extends Reducer>>) module -> {
                        final var reducer = module.getDeclaredConstructor().newInstance();

                        logger.info("Initializing reducer " + module.getName());

                        TempDir.at(NamingStrategy.ForInstance(reducer), tempDir).use(reducerDir ->
                                cache.classes().forEach((TConsumer<String>) fileName -> {
                                    logger.info("Reducing file " + fileName);

                                    // write all other files to the temporary directory
                                    cache.write(reducerDir);

                                    var bytecode = cache.bytecode(fileName);

                                    bytecode = reducer.apply(bytecode, result -> {
                                        var path = reducerDir.resolve(fileName);

                                        try {
                                            Files.write(path, result);
                                        } catch (IOException e) {
                                            logger.fatal(e);
                                        }

                                        // check bytecode validity
                                        var isValid = testSuite.test(reducerDir);

                                        if (isValid) {
                                            cache.update(fileName, result)
                                                    .write(context.outDir);
                                        }

                                        return isValid;
                                    });

                                    cache.update(fileName, bytecode);
                                }), context.keepTemp);
                    }), context.keepTemp);

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
