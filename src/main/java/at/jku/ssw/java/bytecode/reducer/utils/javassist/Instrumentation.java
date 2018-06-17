package at.jku.ssw.java.bytecode.reducer.utils.javassist;

import at.jku.ssw.java.bytecode.reducer.utils.functional.TConsumer;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Javassist utility class that enables simple class instrumentation
 * and allows access on specific methods, call sites etc.
 */
public final class Instrumentation {
    private Instrumentation() {
    }

    /**
     * Performs the given action for each field access of the given class.
     * Results can be filtered.
     *
     * @param clazz  The class under inspection
     * @param filter Only include field access locations which pass this test
     * @param action The action to execute for each field access
     * @throws CannotCompileException if the class cannot be instrumented
     */
    public static void forFieldAccesses(CtClass clazz,
                                        Predicate<FieldAccess> filter,
                                        Consumer<FieldAccess> action)
            throws CannotCompileException {
        clazz.instrument(new ExprEditor() {
            @Override
            public void edit(FieldAccess fa) {
                if (filter.test(fa))
                    action.accept(fa);
            }
        });
    }

    /**
     * Performs the given action for each field access of the given class.
     * Results can be filtered.
     *
     * @param clazz  The class under inspection
     * @param filter Only include method calls that pass this test
     * @param action The action to execute for each method call
     * @throws CannotCompileException if the class cannot be instrumented
     */
    public static void forMethodCalls(CtClass clazz,
                                      Predicate<MethodCall> filter,
                                      Consumer<MethodCall> action)
            throws CannotCompileException {
        clazz.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall mc) {
                if (filter.test(mc))
                    action.accept(mc);
            }
        });
    }

    /**
     * Retrieves the unused fields of the given class.
     * Includes those that match a filter criteria.
     *
     * @param clazz   The containing class
     * @param include A filter to include used fields under certain conditions
     * @return a stream of {@link CtField}s
     * @throws CannotCompileException if the class cannot be instrumented
     */
    public static Stream<CtField> unusedFields(CtClass clazz, Predicate<FieldAccess> include)
            throws CannotCompileException {

        Set<CtField> fields = new HashSet<>(Arrays.asList(clazz.getDeclaredFields()));

        forFieldAccesses(
                clazz,
                fa -> !include.test(fa),
                (TConsumer<FieldAccess>) fa -> fields.remove(fa.getField())
        );

        return fields.stream();
    }

    /**
     * Retrieves the unused methods of the given class.
     * Includes those that match a filter criteria.
     *
     * @param clazz   The containing class
     * @param include A filter to include called methods under certain conditions
     * @return a stream of {@link CtMethod}s
     * @throws CannotCompileException if the class cannot be instrumented
     */
    public static Stream<CtMethod> unusedMethods(CtClass clazz, Predicate<MethodCall> include)
            throws CannotCompileException {

        Set<CtMethod> methods = new HashSet<>(Arrays.asList(clazz.getDeclaredMethods()));

        forMethodCalls(
                clazz,
                mc -> !include.test(mc),
                (TConsumer<MethodCall>) mc -> methods.remove(mc.getMethod()));

        return methods.stream();
    }
}
