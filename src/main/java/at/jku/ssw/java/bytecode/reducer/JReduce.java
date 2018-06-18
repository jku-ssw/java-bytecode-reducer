package at.jku.ssw.java.bytecode.reducer;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.cli.CLIParser;
import at.jku.ssw.java.bytecode.reducer.context.ContextFactory;
import at.jku.ssw.java.bytecode.reducer.errors.DuplicateClassException;
import at.jku.ssw.java.bytecode.reducer.io.NamingStrategy;
import at.jku.ssw.java.bytecode.reducer.io.ScriptRunner;
import at.jku.ssw.java.bytecode.reducer.io.TempDir;
import at.jku.ssw.java.bytecode.reducer.modules.fields.RemoveReadOnlyFields;
import at.jku.ssw.java.bytecode.reducer.modules.fields.RemoveStaticAttributes;
import at.jku.ssw.java.bytecode.reducer.modules.fields.RemoveUnusedFields;
import at.jku.ssw.java.bytecode.reducer.modules.fields.RemoveWriteOnlyFields;
import at.jku.ssw.java.bytecode.reducer.modules.methods.RemoveEmptyMethods;
import at.jku.ssw.java.bytecode.reducer.modules.methods.RemoveUnusedMethods;
import at.jku.ssw.java.bytecode.reducer.runtypes.Reducer;
import at.jku.ssw.java.bytecode.reducer.utils.FileUtils;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TConsumer;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
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
                    RemoveStaticAttributes.class
            );

            final var pre = modules.stream()
                    .filter(c -> c.isAnnotationPresent(Sound.class))
                    .collect(Collectors.toList());

            final var core = modules.stream()
                    .filter(c -> c.isAnnotationPresent(Unsound.class))
                    .collect(Collectors.toList());

            // TODO determine if actually useful
            /*
            Running order: apply sound operations first,
            then try experimental ones that may remove significant portions
            of the code and then run sound ones again to reduce assets that
            may have been affected by the core transformations.
            */
            final var stages = List.of(pre, core, pre);

            // initialize the context
            final var context = contextFactory.createContext();

            final var runner = new ScriptRunner();

            // instantiate the temporary directory at the given location
            TempDir.at(context.tempDir).use(tempDir ->
                    stages.forEach(stage ->
                            stage.forEach((TConsumer<Class<? extends Reducer>>) module -> {
                                final var reducer = module.getDeclaredConstructor().newInstance();

                                logger.info("Initializing reducer " + module.getName());

                                TempDir.at(NamingStrategy.ForInstance(reducer), tempDir).use(reducerDir ->
                                        context.cache.classes().forEach((TConsumer<String>) fileName -> {
                                            logger.info("Reducing file " + fileName);

                                            // write all other files to the temporary directory
                                            context.cache.classes().stream()
                                                    .filter(c -> !c.equals(fileName))
                                                    .forEach(c -> {
                                                        var path     = context.outDir.resolve(c);
                                                        var bytecode = context.cache.bytecode(c);

                                                        try {
                                                            Files.write(path, bytecode);
                                                        } catch (IOException e) {
                                                            logger.fatal(e);
                                                        }
                                                    });

                                            var bytecode = context.cache.bytecode(fileName);

                                            bytecode = reducer.apply(bytecode, result -> {
                                                var path = context.outDir.resolve(fileName);

                                                try {
                                                    Files.write(path, result);
                                                } catch (IOException e) {
                                                    logger.fatal(e);
                                                }

                                                // copy interestingness tests
                                                // and run them
                                                var isValid = FileUtils.copy(context.iTests.stream(), context.outDir)
                                                        .map(itest -> {
                                                            try {
                                                                return runner.exec(itest);
                                                            } catch (IOException e) {
                                                                logger.fatal(e);
                                                                return null;
                                                            }
                                                        })
                                                        .allMatch(p ->
                                                        {
                                                            try {
                                                                return p != null && p.waitFor() == ScriptRunner.EXIT_SUCCESS;
                                                            } catch (InterruptedException e) {
                                                                logger.fatal(e);
                                                                return false;
                                                            }
                                                        });

                                                if (isValid) {
                                                    context.cache.update(fileName, result);
                                                    // TODO write intermediate result to output directory
                                                }

                                                return isValid;
                                            });

                                            context.cache.update(fileName, bytecode);
                                        }), context.keepTemp);
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
