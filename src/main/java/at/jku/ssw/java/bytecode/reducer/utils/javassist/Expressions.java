package at.jku.ssw.java.bytecode.reducer.utils.javassist;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.MethodCall;

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

    /**
     * Determines if the given method call is a recursion on the given method.
     *
     * @param call The method call
     * @return {@code true} if the call invokes itself; {@code false} otherwise
     */
    public static boolean isRecursion(MethodCall call) {
        CtBehavior callSite = call.where();

        try {
            return callSite instanceof CtMethod && call.getMethod().equals(callSite);
        } catch (NotFoundException e) {
            return false;
        }
    }
}
