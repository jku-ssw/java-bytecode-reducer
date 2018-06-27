package at.jku.ssw.java.bytecode.reducer.utils.javassist;

import javassist.CtClass;

import java.util.Map;

/**
 * Javassist helper for real values and expressions.
 */
public final class Expressions {
    private Expressions() {
    }

    private static final Map<CtClass, String> DEFAULTS = Map.of(
            CtClass.byteType, "(byte) 0",
            CtClass.shortType, "(short) 0",
            CtClass.intType, "0",
            CtClass.longType, "0L",
            CtClass.floatType, "0.0F",
            CtClass.doubleType, "0.0",
            CtClass.charType, "'\\0'",
            CtClass.booleanType, "false"
    );

    /**
     * Returns the default value for the given {@link CtClass} instance.
     * This yields the string representation that can be passed on to the
     * Javassist compiler
     *
     * @param type The type for the default value
     * @return the default value for the given type
     */
    public static String defaults(CtClass type) {
        return DEFAULTS.getOrDefault(type, "null");
    }


}
