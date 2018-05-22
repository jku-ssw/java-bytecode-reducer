package at.jku.ssw.java.bytecode.reducer.utils;

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

        FieldAccessVisitor visitor = new FieldAccessVisitor(clazz, include);

        clazz.instrument(visitor);

        return visitor.fields.stream();
    }

    public static Stream<CtMethod> unusedMethods(CtClass clazz, Predicate<MethodCall> include)
            throws CannotCompileException {

        MethodCallVisitor visitor = new MethodCallVisitor(clazz, include);

        clazz.instrument(visitor);

        return visitor.methods.stream();
    }

    private static class FieldAccessVisitor extends ExprEditor {
        private final Set<CtField>           fields;
        private final Predicate<FieldAccess> filter;

        FieldAccessVisitor(CtClass clazz, Predicate<FieldAccess> include) {
            this.fields = new HashSet<>(Arrays.asList(clazz.getDeclaredFields()));
            this.filter = include;
        }

        @Override
        public void edit(FieldAccess f) {
            if (filter.test(f)) return;

            try {
                CtField field = f.getField();

                fields.remove(field);
            } catch (NotFoundException e) {
                // should not happen
                e.printStackTrace();
            }
        }
    }

    private static class MethodCallVisitor extends ExprEditor {
        private final Set<CtMethod>         methods;
        private final Predicate<MethodCall> filter;

        MethodCallVisitor(CtClass clazz, Predicate<MethodCall> include) {
            this.methods = new HashSet<>(Arrays.asList(clazz.getDeclaredMethods()));
            this.filter = include;
        }

        @Override
        public void edit(MethodCall m) {
            if (filter.test(m)) return;

            try {
                CtMethod method = m.getMethod();

                methods.remove(method);
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
