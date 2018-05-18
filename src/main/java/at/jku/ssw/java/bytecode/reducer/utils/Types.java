package at.jku.ssw.java.bytecode.reducer.utils;

import java.util.Map;

public class Types {
    private static final Map<Class<?>, Object> defaults = Map.of(
            byte.class, (byte) 0,
            short.class, (short) 0,
            int.class, 0,
            long.class, 0L,
            float.class, 0.0F,
            double.class, 0.0,
            char.class, '\0',
            boolean.class, Boolean.FALSE
    );

    /**
     * Returns the default value that is automatically a assigned to fields
     * of the given type.
     *
     * @param type The type for the required default value
     * @param <T>  The generic type of the value
     * @return the default value for the given primitive type
     * or null for reference types
     */
    @SuppressWarnings("unchecked")
    public static <T> T defaults(Class<T> type) {
        return (T) defaults.getOrDefault(type, null);
    }
}
