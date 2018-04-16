package at.jku.ssw.java.bytecode.reducer;

import at.jku.ssw.java.bytecode.reducer.context.Context;
import at.jku.ssw.java.bytecode.reducer.io.ScriptRunner;
import at.jku.ssw.java.bytecode.reducer.io.TestDirectory;
import at.jku.ssw.java.bytecode.reducer.runtypes.Reducer;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.ClassFilePrinter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class DeltaTest implements Runnable {
    private static final Logger logger = LogManager.getLogger();

    private final String       name;
    private final Reducer      reducer;
    private final Context      context;
    private final ScriptRunner runner;

    /**
     * The current directory
     */
    private final TestDirectory dir;
    private final ClassPool     cp;

    public DeltaTest(String name, Reducer reducer, Context context) throws IOException {
        this.name = name + "_" + reducer.getClass().getSimpleName();
        this.reducer = reducer;
        this.context = context;
        // create a new test directory
        this.dir = new TestDirectory(context, this.name);
        runner = new ScriptRunner();
        cp = ClassPool.getDefault();
    }

    @Override
    public void run() {
        debug("Initializing reducer");

        // load the individual classes and transform them
        // TODO maybe separate / run tests per reduction on file
        try {
            for (Path classFile : context.classFiles) {
                reduce(classFile);
            }
        } catch (Exception e) {
            fatal("Could not complete reduction: {}", e.getMessage());
            e.printStackTrace();
        }

        debug("Finished reduction", this, reducer);

        // TODO use return values
        if (test()) {
            info("Test successful");
            try {
                if (dir.lift()) {
                    dir.clear();
                } else {
                    fatal("Could not copy result files to output directory");
                }
            } catch (IOException e) {
                fatal("Could not copy result files to output directory: {}", e.getMessage());
            }
        } else {
            info("Test failed");
            dir.clear();
        }

        debug("Test directory cleared");
        debug("{}: Cleared test directory", this);
    }

    @Override
    public String toString() {
        return name;
    }

    private CtClass loadClass(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            CtClass clazz = cp.makeClass(is);
            print(clazz);
            return clazz;
        }
    }

    private void writeClass(CtClass clazz) throws IOException, CannotCompileException, NotFoundException {
        print(clazz);
        clazz.writeFile();
    }

    private void reduce(Path path) throws Exception {
        CtClass clazz = loadClass(path);
//        clazz = reducer.apply();
        writeClass(clazz);
    }

    private boolean test() {
        return context.iTests.stream().map(i -> {
            try {
                return runner.exec(i);
            } catch (IOException e) {
                fatal("Could not complete test, as the script reported errors: {}", e.getMessage());
                return null;
            }
        }).allMatch(p -> {
            try {
                return p != null && p.waitFor() == ScriptRunner.EXIT_SUCCESS;
            } catch (InterruptedException e) {
                fatal("Could not complete test, as the process was interrupted: {}", e.getMessage());
                return false;
            }
        });

    }

    private void print(CtClass clazz) {
        PrintWriter writer = IoBuilder.forLogger()
                .setLevel(Level.DEBUG)
                .buildPrintWriter();
        ClassFilePrinter.print(clazz.getClassFile(), writer);
    }

    private void debug(String message, Object... params) {
        logger.debug(this + ": " + message, params);
    }

    private void info(String message, Object... params) {
        logger.info(this + ": " + message, params);
    }

    private void fatal(String message, Object... params) {
        logger.fatal(this + ": " + message, params);
    }
}
