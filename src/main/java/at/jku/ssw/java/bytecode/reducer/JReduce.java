package at.jku.ssw.java.bytecode.reducer;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.cli.CLIParser;
import at.jku.ssw.java.bytecode.reducer.context.Context;
import at.jku.ssw.java.bytecode.reducer.context.ContextFactory;
import at.jku.ssw.java.bytecode.reducer.io.NamingStrategy;
import at.jku.ssw.java.bytecode.reducer.io.TempDir;
import at.jku.ssw.java.bytecode.reducer.modules.fields.RemoveReadOnlyFields;
import at.jku.ssw.java.bytecode.reducer.modules.fields.RemoveStaticAttributes;
import at.jku.ssw.java.bytecode.reducer.modules.fields.RemoveUnusedFields;
import at.jku.ssw.java.bytecode.reducer.modules.fields.RemoveWriteOnlyFields;
import at.jku.ssw.java.bytecode.reducer.modules.methods.RemoveEmptyMethods;
import at.jku.ssw.java.bytecode.reducer.modules.methods.RemoveUnusedMethods;
import at.jku.ssw.java.bytecode.reducer.runtypes.Reducer;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TConsumer;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
            Context context = contextFactory.createContext();

//            var cache = context.classFiles.stream()
//                .((TFunction<Path, byte[]>)Files::readAllBytes)
//                    .

            // instantiate the temporary directory at the given location
            TempDir.at(context.tempDir).use(tempDir -> {
                stages.forEach(stage -> {
                    stage.forEach((TConsumer<Class<? extends Reducer>>) module -> {
//                        context.classFiles.forEach(file -> {
//                            try {
//                                //
//                                var bytes = Files.readAllBytes(file);
//
//
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        });

                        var reducer = module.getDeclaredConstructor().newInstance();

                        // TODO reducer.apply(bytecode)
//                        if (reducer instanceof RepeatableReducer) {
//                            var bytecode = ((RepeatableReducer) reducer).
//                        }
                    });
                });
                modules.forEach((TConsumer<Class<? extends Reducer>>) c -> {
                    var reducer = c.getDeclaredConstructor().newInstance();

                    TempDir.at(NamingStrategy.ForInstance(reducer), tempDir).use(reducerDir -> {
                        // TODO
                    }, context.keepTemp);
                });
            }, context.keepTemp);

        } catch (ParseException e) {
            logger.fatal(e.getMessage());
            System.exit(ERROR_INVALID_ARGS);
        } catch (IOException e) {
            logger.fatal("Could not initialize working directory: {}", e.getMessage());
        }
    }

}
