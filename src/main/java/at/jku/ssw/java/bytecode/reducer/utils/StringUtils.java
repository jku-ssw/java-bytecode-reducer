package at.jku.ssw.java.bytecode.reducer.utils;

/**
 * Utility methods to extends the functionality of {@link java.lang.String}s.
 */
public final class StringUtils {
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
}
