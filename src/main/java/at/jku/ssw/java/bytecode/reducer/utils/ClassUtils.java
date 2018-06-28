package at.jku.ssw.java.bytecode.reducer.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
    public static Type[] getGenericTypes(Class<?> clazz) {
        return ((ParameterizedType) clazz.getGenericSuperclass())
                .getActualTypeArguments();
    }
}
