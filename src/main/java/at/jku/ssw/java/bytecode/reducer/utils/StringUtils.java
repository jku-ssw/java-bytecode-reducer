package at.jku.ssw.java.bytecode.reducer.utils;

/**
 * Utility methods to extends the functionality of {@link java.lang.String}s.
 */
public final class StringUtils {

    /**
     * Regular expression to identify camel case strings and split
     * them accordingly.
     */
    private static final String CAMEL_CASE_REGEX = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";

    private StringUtils() {
    }

    /**
     * Converts the given camel-cased string to snake-case.
     *
     * @param str The camel-cased string
     * @return a snake-cased string
     */
    public static String snake_case(String str) {
        return String.join("_", str.split(CAMEL_CASE_REGEX));
    }
}
