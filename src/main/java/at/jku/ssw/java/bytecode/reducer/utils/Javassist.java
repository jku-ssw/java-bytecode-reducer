package at.jku.ssw.java.bytecode.reducer.utils;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Utility class that contains helpers for Javassist.
 */
public class Javassist {
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

}
