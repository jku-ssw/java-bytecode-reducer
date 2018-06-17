package at.jku.ssw.java.bytecode.reducer.support;

import at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TFunction;
import javassist.*;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mixin that can be applied to get Javassist test utility methods.
 */
public interface JavassistSupport {

    /**
     * @see Javassist#loadClass(byte[])
     */
    default CtClass classFromBytecode(byte[] bytecode) throws IOException {
        return Javassist.loadClass(bytecode);
    }

    /**
     * @see Javassist#bytecode(CtClass)
     */
    default byte[] bytecodeFromClass(CtClass clazz) throws IOException, CannotCompileException {
        return Javassist.bytecode(clazz);
    }

    default void assertNoFieldAccess(CtClass clazz, String... fields) throws CannotCompileException {
        Consumer<String> assertNoFieldAccess;

        if (fields.length == 0) {
            // fail on any field access
            assertNoFieldAccess = fieldName -> fail(fieldName + "is still accessed");
        } else {
            // fail only when accessing one of the given fields
            assertNoFieldAccess =
                    fieldName -> assertAll(
                            Arrays.stream(fields)
                                    .map(f ->
                                            () -> assertNotEquals(f, fieldName)));
        }

        clazz.instrument(new ExprEditor() {
            @Override
            public void edit(FieldAccess fa) {
                try {
                    var fieldName = fa.getField().getName();

                    assertNoFieldAccess.accept(fieldName);
                } catch (NotFoundException e) {
                    fail(e);
                }
            }
        });
    }

    default void assertNoMethodCall(CtClass clazz, String... methods) throws CannotCompileException {
        Consumer<String> assertNoMethodCall;

        if (methods.length == 0) {
            // no method names given, therefore fail on any invocation
            assertNoMethodCall = methodName -> fail(methodName + "is still called");
        } else {
            // fail only when detecting calls to the given fields
            assertNoMethodCall =
                    methodSign -> assertAll(
                            Arrays.stream(methods)
                                    .map(m ->
                                            () -> assertNotEquals(m, methodSign)));
        }

        clazz.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall mc) {
                try {
                    var methodSignature = mc.getMethod().getLongName();

                    assertNoMethodCall.accept(methodSignature);
                } catch (NotFoundException e) {
                    fail(e);
                }
            }
        });
    }

    default void assertClassEquals(byte[] expected, byte[] actual)
            throws NotFoundException, IOException {

        CtClass expectedClass = classFromBytecode(expected);
        CtClass actualClass   = classFromBytecode(actual);

        assertClassEquals(expectedClass, actualClass);
    }

    @SuppressWarnings("unchecked")
    default void assertClassEquals(CtClass expected, CtClass actual) throws NotFoundException {
        assertNotNull(expected);
        assertNotNull(actual);

        // compare class names
        assertEquals(expected.getName(), actual.getName());

        // compare extended classes
        CtClass expectedDeclaringClass = expected.getDeclaringClass();
        CtClass actualDeclaringClass   = actual.getDeclaringClass();

        if (expectedDeclaringClass == null)
            assertNull(actualDeclaringClass);
        else
            assertEquals(expectedDeclaringClass.getName(), actualDeclaringClass.getName());

        // compare inherited classes
        assertEquals(expected.getSuperclass().getName(), actual.getSuperclass().getName());

        // compare inner / nested classes
        assertArrayEquals(expected.getDeclaredClasses(), actual.getDeclaredClasses(),
                (CtClass a, CtClass b) -> a.getName().equals(b.getName()),
                (a, b) -> assertEquals(a.getName(), b.getName()));

        // compare interfaces
        assertArrayEquals(expected.getInterfaces(), actual.getInterfaces(),
                (CtClass a, CtClass b) -> a.getName().equals(b.getName()),
                (a, b) -> assertEquals(a.getName(), b.getName()));

        // compare annotations
        assertAnnotationEquals(expected, actual,
                (TFunction<CtClass, Object[]>) CtClass::getAnnotations);

        // compare attributes
        assertCollectionEquals(
                expected.getClassFile().getAttributes(),
                actual.getClassFile().getAttributes(),
                (AttributeInfo a, AttributeInfo b) -> a.getName().equals(b.getName()),
                (a, b) -> assertEquals(a.getName(), b.getName()));

        assertArrayEquals(
                expected.getDeclaredFields(),
                actual.getDeclaredFields(),
                (CtField a, CtField b) -> a.getName().equals(b.getName()),
                this::assertFieldEquals);

        assertArrayEquals(
                expected.getDeclaredBehaviors(),
                actual.getDeclaredBehaviors(),
                (CtBehavior a, CtBehavior b) -> a.getLongName().equals(b.getLongName()),
                this::assertBehaviourEquals);
    }

    /**
     * Utility methods to compare member arrays (e.g. classes, fields).
     * This comparison is only shallow as the only modified object
     * is usually the declaring class / member.
     *
     * @param a1        The first component's attributes
     * @param a2        The second component's attributes
     * @param filter    Function to filter the applicable results for
     *                  the comparator
     * @param assertion Function to determine whether an attribute of
     *                  one component is also an attribute of the other
     * @param <T>       The type of the compared attributes
     * @see JavassistSupport#assertCollectionEquals(Collection, Collection, BiPredicate, BiConsumer)
     */
    @SuppressWarnings("unchecked")
    private <T> void assertArrayEquals(Object[] a1,
                                       Object[] a2,
                                       BiPredicate<T, T> filter,
                                       BiConsumer<T, T> assertion) {

        assertCollectionEquals(List.of(a1), List.of(a2), filter, assertion);
    }

    /**
     * Utility method to compare member lists (classes, fields, annotations).
     * This comparison is only shallow as the only modified object
     * is usually the declaring class / member.
     *
     * @param l1        The first list
     * @param l2        The second list
     * @param filter    Filter method to get an applicable result
     *                  for the comparison
     * @param assertion The comparator method (assertion)
     * @param <T>       Generic type of the attribute
     */
    @SuppressWarnings("unchecked")
    private <T> void assertCollectionEquals(Collection l1,
                                            Collection l2,
                                            BiPredicate<T, T> filter,
                                            BiConsumer<T, T> assertion) {

        assertEquals(l1.size(), l2.size());
        assertAll(((Collection<T>) l1).stream().map(a ->
                () -> assertion.accept(
                        a,
                        ((Collection<T>) l2).stream()
                                .filter(b -> filter.test(a, b))
                                .findAny()
                                .orElse(null)
                )
        ));
    }


    @SuppressWarnings("unchecked")
    default void assertFieldEquals(CtField expected, CtField actual) {

        assertEquals(expected.getName(), actual.getName());

        try {
            assertEquals(expected.getType(), actual.getType());
        } catch (NotFoundException e) {
            fail(e);
        }

        assertEquals(expected.getConstantValue(), actual.getConstantValue());

        assertEquals(expected.getModifiers(), actual.getModifiers());

        assertCollectionEquals(
                expected.getFieldInfo().getAttributes(),
                expected.getFieldInfo().getAttributes(),
                (AttributeInfo a, AttributeInfo b) -> a.getName().equals(b.getName()),
                (a, b) -> assertEquals(a.getName(), b.getName()));

        assertAnnotationEquals(expected, actual,
                (TFunction<CtField, Object[]>) CtField::getAnnotations);
    }

    default void assertAnnotationEquals(Annotation expected, Annotation actual) {
        assertEquals(expected.getTypeName(), actual.getTypeName());

        assertCollectionEquals(
                expected.getMemberNames(),
                actual.getMemberNames(),
                String::equals,
                (a, b) -> assertEquals(expected.getMemberValue(a), actual.getMemberValue(b)));
    }

    private <T> void assertAnnotationEquals(T a, T b, Function<T, Object[]> prop) {

        assertArrayEquals(prop.apply(a), prop.apply(b),
                (Annotation a1, Annotation a2) -> a1.getTypeName().equals(a2.getTypeName()),
                this::assertAnnotationEquals
        );
    }

    default void assertBehaviourEquals(CtBehavior expected, CtBehavior actual) {
        // compare names
        assertEquals(expected.getLongName(), actual.getLongName());

        // compare signature
        assertEquals(expected.getSignature(), actual.getSignature());

        // compare method annotations
        assertAnnotationEquals(expected, actual,
                (TFunction<CtBehavior, Object[]>) CtBehavior::getAnnotations);

        // compare modifiers
        assertEquals(expected.getModifiers(), actual.getModifiers());

        // compare parameter annotations
        try {
            assertAll(IntStream.range(0, expected.getParameterTypes().length)
                    .mapToObj(i ->
                            () -> assertAnnotationEquals(
                                    expected.getParameterAnnotations(),
                                    actual.getParameterAnnotations(),
                                    (TFunction<Object[][], Object[]>) arr -> arr[i]))
            );
        } catch (NotFoundException e) {
            fail(e);
        }

        // compare declared exceptions
        try {
            assertArrayEquals(
                    expected.getExceptionTypes(),
                    actual.getExceptionTypes(),
                    (CtClass a, CtClass b) -> a.getName().equals(b.getName()),
                    (a, b) -> assertEquals(a.getName(), b.getName()));
        } catch (NotFoundException e) {
            fail(e);
        }

        // detailed comparison based on actual subtype
        if (expected instanceof CtMethod) {
            assertTrue(actual instanceof CtMethod);
            assertMethodEquals((CtMethod) expected, (CtMethod) actual);
        } else if (expected instanceof CtConstructor) {
            assertTrue(actual instanceof CtConstructor);
            // no other comparison required for constructors
        } else {
            fail(() -> String.format("\'%s\' is unknown %s subtype",
                    expected.getClass().getName(),
                    CtBehavior.class.getName()));
        }

        // compare method bodies
        assertMethodInfoEquals(expected.getMethodInfo(), actual.getMethodInfo());
    }

    default void assertMethodEquals(CtMethod expected, CtMethod actual) {
        try {
            // compare return type
            // (as void methods return CtClass#voidType, a comparison by name
            // should be sufficient)
            assertEquals(expected.getReturnType().getName(),
                    actual.getReturnType().getName());
        } catch (NotFoundException e) {
            fail(e);
        }
    }

    private void assertMethodInfoEquals(MethodInfo expected, MethodInfo actual) {
        // TODO
        /*
            This currently does not work as some reduction operations
            produce inherently inconsistent bytecode.
            A instruction-by-instruction comparison is therefore impossible
        */
//        CodeIterator itExpected = expected.getCodeAttribute().iterator();
//        CodeIterator itActual   = actual.getCodeAttribute().iterator();
//
//        try {
//            while (itExpected.hasNext()) {
//                // ensure that both have a successor
//                assertTrue(itActual.hasNext());
//
//                // bytecode indices
//                int iExpected = itExpected.next();
//                int iActual   = itActual.next();
//
//                // actual op codes
//                int opExpected = itExpected.byteAt(iExpected);
//                int opActual   = itActual.byteAt(iActual);
//
//                // compare operations
//                assertEquals(opExpected, opActual);
//            }
//        } catch (BadBytecode e) {
//            fail(e);
//        }
//
//        // ensure that both iterators have been exhausted
//        assertFalse(itActual.hasNext());
    }
}
