package at.jku.ssw.java.bytecode.reducer.utils.javassist;

import at.jku.ssw.java.bytecode.reducer.utils.functional.Catch;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.expr.*;

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
     * Performs the given action for each constructor call
     * (e.g. {@code super()}, {@code this()} while filtering the results.
     *
     * @param clazz  The class to instrument
     * @param filter Only include constructor calls that pass this test
     * @param action The action to execute for each constructor call
     * @throws CannotCompileException if the class cannot be instrumented
     */
    public static void forConstructorCalls(CtClass clazz,
                                           Predicate<ConstructorCall> filter,
                                           Consumer<ConstructorCall> action)
            throws CannotCompileException {
        clazz.instrument(new ExprEditor() {
            @Override
            public void edit(ConstructorCall c) {
                if (filter.test(c))
                    action.accept(c);
            }
        });
    }

    /**
     * Performs the given action for each {@code new} expression that
     * passes the given filter.
     *
     * @param clazz  The class to instrument
     * @param filter Only include expressions that pass this test
     * @param action The action to execute for each such expression
     * @throws CannotCompileException if the class cannot be instrumented
     */
    public static void forNewExpressions(CtClass clazz,
                                         Predicate<NewExpr> filter,
                                         Consumer<NewExpr> action)
            throws CannotCompileException {
        clazz.instrument(new ExprEditor() {
            @Override
            public void edit(NewExpr e) {
                if (filter.test(e))
                    action.accept(e);
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
                include.negate(),
                Catch.consumer(fa -> fields.remove(fa.getField()))
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
                include.negate(),
                Catch.consumer(mc -> methods.remove(mc.getMethod())));

        return methods.stream();
    }
}
