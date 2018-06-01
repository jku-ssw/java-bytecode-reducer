package at.jku.ssw.java.bytecode.reducer;

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

            // No context received - exit
            if (contextFactory == null)
                System.exit(EXIT_SUCCESS);

            // TODO invoke by loading class path files or specify otherwise
            final List<Class<? extends Reducer>> modules = List.of(
                    RemoveUnusedFields.class,
                    RemoveUnusedMethods.class,
                    RemoveWriteOnlyFields.class,
                    RemoveEmptyMethods.class,
                    RemoveReadOnlyFields.class,
                    RemoveStaticAttributes.class
            );

            final List<Class<? extends Reducer>> l = modules.stream()
                    .sorted(Reducer.ORDERING)
                    .collect(Collectors.toList());

            // TODO relocate to other class / make reusable ("better")

            // TODO iterate over
            // TODO maybe invoke in ThreadPool

            Context context = contextFactory.createContext();

            TempDir.at(context.tempDir).use(tempDir -> {
                modules.forEach((TConsumer<Class<? extends Reducer>>) c -> {
                    Reducer reducer = c.getDeclaredConstructor().newInstance();

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
