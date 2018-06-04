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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Superclass for {@link Reducer} tests.
 * Simplifies the access to byte codes / resources and keeps track
 * of a reducer's individual resources.
 *
 * @param <T> The type of the {@link Reducer} to test
 */
public abstract class ReducerTest<T extends Reducer> implements JavassistSupport {

    /**
     * The directory name for byte codes.
     */
    public final String BYTE_CODE_DIR_NAME = "bytecodes";

    /**
     * The directory name for the source byte codes.
     */
    public final String ORIGINAL_DIR_NAME = "original";

    /**
     * The directory name for reduced / expected result byte codes.
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
        Class<T> reducer = (Class<T>) ClassUtils.getGenericTypes(getClass())[0];

        String dirName   = StringUtils.snake_case(reducer.getSimpleName());
        String resources = BYTE_CODE_DIR_NAME + File.separator + dirName + File.separator;
        originalResources = resources + ORIGINAL_DIR_NAME + File.separator;
        reducedResources = resources + REDUCED_DIR_NAME + File.separator;
    }

    /**
     * Load the original class identified by the given name.
     *
     * @param name The class name
     * @return the byte code of the requested class
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
     * @return the byte code of the requested class
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
        System.out.println("Loading " + path + " as class path resource");
        System.out.println("Current directory is " + Paths.get(".").toAbsolutePath());
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
        Files.walkFileTree(Paths.get("."), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                System.out.println("Directory: " + dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                System.out.println("File: " + file);
                return FileVisitResult.CONTINUE;
            }
        });

        byte[] original = loadOriginalBytecode(className);

        byte[] expected = loadReducedBytecode(className);

        byte[] actual = reducer.apply(original);

        // TODO remove
//        CtClass expected = classFromBytecode(expectedBytecode);
//        CtClass actual   = classFromBytecode(reducedBytecode);

//        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(Paths.get("").resolve(className + ".class").toFile()))) {
//            actual.getClassFile().write(out);
//        }

        assertClassEquals(expected, actual);

        return ContinueAssertion.with(actual);
    }

}
