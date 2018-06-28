package at.jku.ssw.java.bytecode.reducer.modules;

import at.jku.ssw.java.bytecode.reducer.runtypes.Reducer;
import at.jku.ssw.java.bytecode.reducer.support.ContinueAssertion;
import at.jku.ssw.java.bytecode.reducer.support.JavassistSupport;
import at.jku.ssw.java.bytecode.reducer.utils.ClassUtils;
import at.jku.ssw.java.bytecode.reducer.utils.StringUtils;
import javassist.CannotCompileException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Superclass for {@link Reducer} tests.
 * Simplifies the access to bytecodes / resources and keeps track
 * of a reducer's individual resources.
 *
 * @param <T> The type of the {@link Reducer} to test
 */
public abstract class ReducerTest<T extends Reducer> implements JavassistSupport {

    /**
     * The directory name for bytecodes.
     */
    public final String BYTE_CODE_DIR_NAME = "bytecodes";

    /**
     * The directory name for the source bytecodes.
     */
    public final String ORIGINAL_DIR_NAME = "original";

    /**
     * The directory name for reduced / expected result bytecodes.
     */
    public final String REDUCED_DIR_NAME = "reduced";

    /**
     * Postfix for class files.
     */
    public final String CLASS_POSTFIX = "class";

    /**
     * Path to original resources.
     */
    private final String originalResources;

    /**
     * Path to reduced resources.
     */
    private final String reducedResources;

    /**
     * The reducer instance to use.
     */
    protected T reducer;

    /**
     * Instantiate a new test and determine the resource paths accordingly.
     */
    @SuppressWarnings("unchecked")
    public ReducerTest() {
        var reducer = (Class<T>) ClassUtils.getGenericTypes(getClass())[0];

        var dirName   = StringUtils.snake_case(reducer.getSimpleName()).toLowerCase();
        var resources = BYTE_CODE_DIR_NAME + File.separator + dirName + File.separator;
        originalResources = resources + ORIGINAL_DIR_NAME + File.separator;
        reducedResources = resources + REDUCED_DIR_NAME + File.separator;
    }

    /**
     * Load the original class identified by the given name.
     *
     * @param name The class name
     * @return the bytecode of the requested class
     * @throws IOException if the file cannot be found
     */
    protected final byte[] loadOriginalBytecode(String name) throws IOException {
        try (InputStream is = getResourceStream(originalResources + name + "." + CLASS_POSTFIX)) {
            return is.readAllBytes();
        }
    }

    /**
     * Load the expected reduced class identified by the given name.
     *
     * @param name The class name
     * @return the bytecode of the requested class
     * @throws IOException if the file cannot be found
     */
    protected final byte[] loadReducedBytecode(String name) throws IOException {
        try (InputStream is = getResourceStream(reducedResources + name + "." + CLASS_POSTFIX)) {
            return is.readAllBytes();
        }
    }

    /**
     * Fetch the resource at the given class path location.
     *
     * @param path The path of the resource
     * @return an input stream for the resource file
     */
    private InputStream getResourceStream(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    protected void assertNoFieldAccess(byte[] clazz, String... fields)
            throws IOException, CannotCompileException {
        assertNoFieldAccess(classFromBytecode(clazz), fields);
    }

    protected void assertNoMethodCall(byte[] clazz, String... methods)
            throws IOException, CannotCompileException {
        assertNoMethodCall(classFromBytecode(clazz), methods);
    }

    protected ContinueAssertion assertReduced(final String className) throws Exception {
        return assertReduced(className, false);
    }

    protected ContinueAssertion assertReduced(final String className, boolean compareBodies) throws Exception {
        var original = loadOriginalBytecode(className);

        var expected = loadReducedBytecode(className);

        var actual = reducer.apply(original);

        assertClassEquals(expected, actual, compareBodies);

        return ContinueAssertion.with(actual);
    }

}
