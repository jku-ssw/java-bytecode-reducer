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
     * Checks if the given string is blank.
     *
     * @param str The string to test
     * @return {@code true} if the string is {@code null} or empty;
     * {@code false} otherwise
     */
    public static boolean isBlank(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Checks if the given string is not blank.
     *
     * @param str The string to test
     * @return {@code true} if the string is neither {@code null} nor empty;
     * {@code false} otherwise
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
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
