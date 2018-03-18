package at.jku.ssw.java.bytecode.reducer;

import at.jku.ssw.java.bytecode.reducer.context.Context;
import at.jku.ssw.java.bytecode.reducer.io.ScriptRunner;
import at.jku.ssw.java.bytecode.reducer.io.TestDirectory;
import at.jku.ssw.java.bytecode.reducer.reducers.Reducer;
import javassist.CtClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class DeltaTest implements Runnable {
    private static final Logger logger = LogManager.getLogger();

    private final Reducer       reducer;
    private final Context       context;
    private final ScriptRunner  runner;
    private final TestDirectory dir;

    public DeltaTest(String name, Reducer reducer, Context context) throws IOException {
        this.reducer = reducer;
        this.context = context;
        // create a new test directory
        this.dir = new TestDirectory(context, name + "_" + reducer);
        runner = new ScriptRunner();
    }

    @Override
    public void run() {
        logger.info("{}: Initializing test using Reducer {}", this, reducer);
//        // TODO
//        context.classFiles.stream()
//                .map(this::loadClass)
//                .peek(reducer::transform)
//                .map(this::writeClass);
//
//        final CtClass clazz = loadClass();
//        reducer.transform(clazz);
        logger.info("{}: Finished reduction {}", this, reducer);

        try {
            if (!finish()) {
                logger.fatal("{}: Reduction finished but file access failed");
            }
        } catch (IOException e) {
            logger.fatal("ERROR: Could not finish test {}: {}", this, e.getMessage());
        }

        logger.info("{}: Cleared test directory", this);
    }

    @Override
    public String toString() {
        return dir.toString();
    }

    private CtClass loadClass(Path path) {

        return null;
    }

    private boolean writeClass(CtClass clazz) {

        return false;
    }

    private synchronized boolean finish() throws IOException {
        return dir.lift() && dir.clear();
    }
}
