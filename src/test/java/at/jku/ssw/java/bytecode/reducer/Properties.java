package at.jku.ssw.java.bytecode.reducer;

/**
 * Holds properties / constants for the tests.
 */
public interface Properties {

    /**
     * The test directory that is used whenever output is generated.
     */
    String DIR = ".test";

    /**
     * Specifies whether all tests should be run in debug mode. This
     * prints extra information for the developer and also
     * redirects larger outputs to corresponding files within the test
     * directory.
     */
    boolean DEBUG = true;
}
