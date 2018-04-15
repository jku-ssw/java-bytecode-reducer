package at.jku.ssw.java.bytecode.reducer.utils;

import java.lang.reflect.ParameterizedType;

/**
 * Utility methods for {@link Class} based operations.
 */
public class ClassUtils {

    /**
     * Returns the generic types of a given class.
     *
     * @param clazz The class to analyze
     * @return an array containing the generic types
     */
    public static Class<?>[] getGenericTypes(Class<?> clazz) {
        return (Class<?>[]) ((ParameterizedType) clazz.getGenericSuperclass())
                .getActualTypeArguments();
    }
}
