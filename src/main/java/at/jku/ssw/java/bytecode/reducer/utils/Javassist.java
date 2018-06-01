package at.jku.ssw.java.bytecode.reducer.utils;

import at.jku.ssw.java.bytecode.reducer.utils.functional.TConsumer;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Utility class that contains helpers for Javassist.
 */
public class Javassist {
    private static final Map<CtClass, String> defaults = Map.of(
            CtClass.byteType, "(byte) 0",
            CtClass.shortType, "(short) 0",
            CtClass.intType, "0",
            CtClass.longType, "0L",
            CtClass.floatType, "0.0F",
            CtClass.doubleType, "0.0",
            CtClass.charType, "'\0'",
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
        return defaults.getOrDefault(type, "null");
    }

    /**
     * Signature of the main method.
     */
    public static final String MAIN_SIGNATURE = "([Ljava/lang/String;)V";

    private Javassist() {
    }

    /**
     * Determine if the given member is declared in the given class.
     *
     * @param member The member (field or method)
     * @param clazz  The (probably) declaring class
     * @return {@code true} if the class declares the member; {@code false} otherwise
     */
    public static boolean isMemberOfClass(CtMember member, CtClass clazz) {
        return clazz.equals(member.getDeclaringClass());
    }

    /**
     * Determine if the given member is a constructor or static initializer.
     *
     * @param member The member that may be a constructor
     * @return {@code true} if the member is a constructor or
     * static initializer; {@code false} otherwise
     */
    public static boolean isInitializer(CtMember member) {
        return member instanceof CtConstructor;
    }

    /**
     * Determines if the given member is a main method.
     *
     * @param member The member to analyze
     * @return {@code true} if the member is a main method; {@code false} if
     * the member is not a method or lacks the main signature
     */
    public static boolean isMain(CtMember member) {
        if (member == null)
            return false;

        final int mod = member.getModifiers();

        return member instanceof CtMethod &&
                Modifier.isStatic(mod) &&
                Modifier.isPublic(mod) &&
                MAIN_SIGNATURE.equals(member.getSignature());
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

    /**
     * Loads the class from the given bytes (that should contain a class file).
     *
     * @param bytecode A byte array that describes the byte code of a class
     * @return the {@link CtClass} corresponding to the byte code
     * @throws IOException if the byte code is invalid
     */
    public static CtClass loadClass(byte[] bytecode) throws IOException {
        try (InputStream is = new ByteArrayInputStream(bytecode)) {
            return ClassPool.getDefault().makeClass(is);
        }
    }

    /**
     * Retrieves the byte code of the given class without locking the instance.
     *
     * @param clazz The class whose byte code is retrieved
     * @return the byte code that represents the given class
     * @throws IOException            if the byte code cannot be generated
     * @throws CannotCompileException if the class does not compile
     */
    public static byte[] bytecode(CtClass clazz)
            throws IOException, CannotCompileException {
        byte[] bytecode = clazz.toBytecode();
        clazz.defrost();
        return bytecode;
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
    public static void methodCalls(CtClass clazz,
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

        methodCalls(
                clazz,
                mc -> !include.test(mc),
                (TConsumer<MethodCall>) mc -> methods.remove(mc.getMethod()));

        return methods.stream();
    }

}
