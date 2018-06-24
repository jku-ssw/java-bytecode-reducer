package at.jku.ssw.java.bytecode.reducer.context;

import at.jku.ssw.java.bytecode.reducer.io.ScriptRunner;
import at.jku.ssw.java.bytecode.reducer.utils.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * Class that keeps track of interestingness tests and provides
 * methods to run those in a given directory.
 */
public class TestSuite {

    private static final Logger logger = LogManager.getLogger();

    /**
     * The individual test scripts that have to be passed
     * in order for a reduction result to be "interesting"-
     */
    private final Set<Path> iTests;

    private final ScriptRunner scriptRunner;

    TestSuite(Set<Path> iTests) {
        this.iTests = iTests;
        scriptRunner = new ScriptRunner();
    }

    public final boolean test(Path testDir) {
        return FileUtils.copy(iTests.stream(), testDir)
                .allMatch(itest -> {
                    try {
                        if (scriptRunner.execBlocking(itest))
                            return true;

                        logger.info("Test " + itest.getFileName() + " failed");

                        return false;
                    } catch (IOException | InterruptedException e) {
                        logger.fatal(e);
                        return false;
                    }
                });
    }
}
