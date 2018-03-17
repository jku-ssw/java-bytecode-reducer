package at.jku.ssw.java.bytecode.reducer.utils;

/**
 * Utility methods to extends the functionality of {@link String}s.
 */
public class StringUtils {
    public static boolean isBlank(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
