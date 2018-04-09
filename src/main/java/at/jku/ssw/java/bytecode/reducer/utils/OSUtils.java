package at.jku.ssw.java.bytecode.reducer.utils;

/**
 * Utility class for OS specific checks.
 */
public final class OSUtils {
    private OSUtils() {}

    /**
     * Determine whether the application is running on
     * in a Windows environment.
     *
     * @return true if the OS is windows; false otherwise
     */
    public static boolean isWindows() {
        return System.getProperty("os.name")
                .toLowerCase()
                .startsWith("windows");
    }
}
