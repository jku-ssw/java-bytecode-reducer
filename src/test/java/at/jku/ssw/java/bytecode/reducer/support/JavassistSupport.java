package at.jku.ssw.java.bytecode.reducer.support;

import at.jku.ssw.java.bytecode.reducer.utils.functional.TFunction;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist;
import javassist.*;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
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
     * Assertion that verifies that the given field(s) are not accessed within
     * the given class. If no fields are given, any field access is treated as
     * a violation.
     *
     * @param clazz  The class to verify
     * @param fields The fields that must not be accessed
     * @throws CannotCompileException if the class instrumentation
     *                                (for the checks) fails
     */
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
                                            () -> assertNotEquals(
                                                    f,
                                                    fieldName,
                                                    "The given field must not be accessed"
                                            )
                                    )
                    );
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

    /**
     * Assertion that verifies that the given method(s) are not called within
     * the given class. If no methods are given, any method call is treated
     * as a violation.
     *
     * @param clazz   The class to verify
     * @param methods The methods that must not be called
     * @throws CannotCompileException if the class instrumentation
     *                                (for the checks) fails
     */
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
                                            () -> assertNotEquals(
                                                    m,
                                                    methodSign,
                                                    "The given method must not be called"
                                            )
                                    )
                    );
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

    /**
     * Verifies that the given bytecodes contain "the same" class.
     * Classes are treated as equal if their whole structure matches.
     * A line by line comparison of method / constructor bodies
     * is NOT performed.
     *
     * @param expected      The expected bytecode
     * @param actual        The actual reduction result
     * @param compareBodies Should method bodies also be compared?
     * @throws NotFoundException if the bytecodes contain an invalid class
     * @throws IOException       if the bytecodes cannot be read
     */
    default void assertClassEquals(byte[] expected, byte[] actual, boolean compareBodies)
            throws NotFoundException, IOException {

        CtClass expectedClass = classFromBytecode(expected);
        CtClass actualClass   = classFromBytecode(actual);

        assertClassEquals(expectedClass, actualClass, compareBodies);
    }

    /**
     * @see JavassistSupport#assertClassEquals(byte[], byte[], boolean)
     */
    @SuppressWarnings("unchecked")
    default void assertClassEquals(CtClass expected, CtClass actual, boolean compareBodies)
            throws NotFoundException {
        assertNotNull(expected, "The class pattern must not be null");
        assertNotNull(actual, "The reduced class must not be null");

        // compare class names
        assertEquals(expected.getName(), actual.getName(), "The class names must match");

        // compare extended classes
        CtClass expectedDeclaringClass = expected.getDeclaringClass();
        CtClass actualDeclaringClass   = actual.getDeclaringClass();

        if (expectedDeclaringClass == null)
            assertNull(actualDeclaringClass, "The reduced class is declared in another class while the pattern is not");
        else
            assertEquals(
                    expectedDeclaringClass.getName(),
                    actualDeclaringClass.getName(),
                    "The declaring classes must be equal"
            );

        // compare inherited classes
        assertEquals(
                expected.getSuperclass().getName(),
                actual.getSuperclass().getName(),
                "The superclasses must match"
        );

        // compare inner / nested classes
        assertArrayEquals(
                expected.getDeclaredClasses(),
                actual.getDeclaredClasses(),
                (CtClass a, CtClass b) -> a.getName().equals(b.getName()),
                (a, b) -> {
                    assertNotNull(b, () -> "Inner class " + a.getName() + " must also exist in the reduced class");
                    assertEquals(a.getName(), b.getName(), "The inner classes must be equal");
                }
        );

        // compare interfaces
        assertArrayEquals(
                expected.getInterfaces(),
                actual.getInterfaces(),
                (CtClass a, CtClass b) -> a.getName().equals(b.getName()),
                (a, b) -> {
                    assertNotNull(b, () -> "Interface " + a.getName() + " must also be implemented in the reduced class");
                    assertEquals(a.getName(), b.getName(), "The implemented interfaces must match");
                }
        );

        // compare annotations
        assertAnnotationEquals(
                expected,
                actual,
                (TFunction<CtClass, Object[]>) CtClass::getAnnotations
        );

        // compare attributes
        assertCollectionEquals(
                expected.getClassFile().getAttributes(),
                actual.getClassFile().getAttributes(),
                (AttributeInfo a, AttributeInfo b) -> a.getName().equals(b.getName()),
                (a, b) -> {
                    assertNotNull(b, () -> "Attribute " + a.getName() + " must also be valid for the reduced class");
                    assertEquals(a.getName(), b.getName(), "The class attributes must match");
                }
        );

        // compare fields
        assertArrayEquals(
                expected.getDeclaredFields(),
                actual.getDeclaredFields(),
                (CtField a, CtField b) -> a.getName().equals(b.getName()),
                this::assertFieldEquals
        );

        // compare "behaviours" (initializers and constructors)
        assertArrayEquals(
                expected.getDeclaredBehaviors(),
                actual.getDeclaredBehaviors(),
                (CtBehavior a, CtBehavior b) -> a.getLongName().equals(b.getLongName()),
                this::assertBehaviourEquals
        );

        if (compareBodies)
            assertClassBodyEquals(expected, actual);
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

    /**
     * Ensures that the given fields are equal.
     *
     * @param expected The expected field
     * @param actual   The actually resulting field
     */
    @SuppressWarnings("unchecked")
    default void assertFieldEquals(CtField expected, CtField actual) {
        var fieldName = expected.getName();

        assertNotNull(actual, () -> "Field " + fieldName + " must exist in the reduced class");
        assertEquals(fieldName, actual.getName(), "The field names must match");

        try {
            assertEquals(expected.getType(), actual.getType(), () -> "The types of field " + fieldName + " must match");
        } catch (NotFoundException e) {
            fail(e);
        }

        assertEquals(expected.getConstantValue(), actual.getConstantValue(), "Constant values of field " + fieldName + " have to be equal");

        assertEquals(expected.getModifiers(), actual.getModifiers(), () -> "The modifiers of field " + fieldName + " have to be equal");

        assertCollectionEquals(
                expected.getFieldInfo().getAttributes(),
                expected.getFieldInfo().getAttributes(),
                (AttributeInfo a, AttributeInfo b) -> a.getName().equals(b.getName()),
                (a, b) -> {
                    assertNotNull(b, () -> "Field attribute " + a.getName() + " of field " + fieldName + " must also be present in the reduced class");
                    assertEquals(a.getName(), b.getName(), () -> "The field attributes of field " + fieldName + " have to match");
                }
        );

        assertAnnotationEquals(
                expected,
                actual,
                (TFunction<CtField, Object[]>) CtField::getAnnotations
        );
    }

    /**
     * Ensures that annotations (of fields, methods, classes) and their
     * parameters match.
     *
     * @param expected The expected annotation
     * @param actual   The actual annotation object
     */
    default void assertAnnotationEquals(Annotation expected, Annotation actual) {
        assertEquals(expected.getTypeName(), actual.getTypeName());

        assertCollectionEquals(
                expected.getMemberNames(),
                actual.getMemberNames(),
                String::equals,
                (a, b) -> {
                    assertNotNull(b, () -> "Member " + a + " of annotation " + expected.toString() + " must also be present in the reduced class");
                    assertEquals(expected.getMemberValue(a), actual.getMemberValue(b), () -> "The values of member " + a + " of annotation " + expected.toString() + " have to be equal");
                }
        );
    }

    /**
     * @see JavassistSupport#assertAnnotationEquals(Annotation, Annotation)
     */
    private <T> void assertAnnotationEquals(T a, T b, Function<T, Object[]> prop) {

        assertArrayEquals(prop.apply(a), prop.apply(b),
                (Annotation a1, Annotation a2) -> a1.getTypeName().equals(a2.getTypeName()),
                this::assertAnnotationEquals
        );
    }

    /**
     * Compares the given class behaviours (methods, initializers).
     *
     * @param expected The expected behaviour
     * @param actual   The actually resulting behaviour
     */
    default void assertBehaviourEquals(CtBehavior expected, CtBehavior actual) {
        var behavName = expected.getLongName();

        // compare names
        assertEquals(
                behavName,
                actual.getLongName(),
                "The behaviour names have to be equal"
        );

        // compare signature
        assertEquals(
                expected.getSignature(),
                actual.getSignature(),
                () -> "The signatures of behaviour " + behavName + " must be equal"
        );

        // compare method annotations
        assertAnnotationEquals(
                expected,
                actual,
                (TFunction<CtBehavior, Object[]>) CtBehavior::getAnnotations
        );

        // compare modifiers
        assertEquals(
                expected.getModifiers(),
                actual.getModifiers(),
                () -> "The modifiers of behaviour " + behavName + " must be equal"
        );

        // compare parameter annotations
        try {
            assertAll(
                    IntStream.range(0, expected.getParameterTypes().length)
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
                    (a, b) -> {
                        assertNotNull(b, () -> "Exception " + a.getName() + " thrown by behaviour " + behavName + " must also be thrown in reduced class");
                        assertEquals(a.getName(), b.getName(), () -> "The thrown exceptions of behaviour " + behavName + " must be equal");
                    });
        } catch (NotFoundException e) {
            fail(e);
        }

        // detailed comparison based on actual behaviour type
        if (expected instanceof CtMethod) {
            // both must be methods
            assertTrue(actual instanceof CtMethod, () -> "Behaviour " + behavName + " must be a method in both classes");
            assertMethodEquals((CtMethod) expected, (CtMethod) actual);
        } else if (expected instanceof CtConstructor) {
            // both must be initializers (static or constructor)

            assertTrue(actual instanceof CtConstructor, () -> "Behaviour " + behavName + " must be an initializer in both classes");
            var expectedConstructor = (CtConstructor) expected;
            var actualConstructor   = (CtConstructor) actual;

            // compare initializer types
            if (expectedConstructor.isClassInitializer())
                assertTrue(
                        actualConstructor.isClassInitializer(),
                        () -> "Initializer " + behavName + " must be a static initializer"
                );
            else if (expectedConstructor.isConstructor())
                assertTrue(
                        actualConstructor.isConstructor(),
                        () -> "Initializer " + behavName + " must be a constructor"
                );
        } else {
            fail(() -> String.format("\'%s\' is unknown %s subtype",
                    expected.getClass().getName(),
                    CtBehavior.class.getName()));
        }
    }

    /**
     * Compares the given methods.
     *
     * @param expected The expected method
     * @param actual   The actual method
     */
    default void assertMethodEquals(CtMethod expected, CtMethod actual) {
        try {
            // compare return type
            // (as void methods return CtClass#voidType, a comparison by name
            // should be sufficient)
            assertEquals(
                    expected.getReturnType().getName(),
                    actual.getReturnType().getName(),
                    () -> "The return types of method " + expected.getLongName() + " must be equal"
            );
        } catch (NotFoundException e) {
            fail(e);
        }
    }

    default void assertClassBodyEquals(CtClass expected, CtClass actual) {
        assertArrayEquals(
                expected.getDeclaredBehaviors(),
                actual.getDeclaredBehaviors(),
                (CtBehavior a, CtBehavior b) -> a.getLongName().equals(b.getLongName()),
                this::assertBehaviourBody
        );
    }

    private void assertBehaviourBody(CtBehavior expected, CtBehavior actual) {
        var expectedInfo = expected.getMethodInfo();
        var actualInfo   = actual.getMethodInfo();

        var itExpected = expectedInfo.getCodeAttribute().iterator();
        var itActual   = actualInfo.getCodeAttribute().iterator();

        try {
            while (itExpected.hasNext()) {
                // ensure that both have a successor
                assertTrue(itActual.hasNext());

                // bytecode indices
                int iExpected = itExpected.next();
                int iActual   = itActual.next();

                // actual op codes
                int opExpected = itExpected.byteAt(iExpected);
                int opActual   = itActual.byteAt(iActual);

                var instrExpected = Mnemonic.OPCODE[opExpected];
                var instrActual   = Mnemonic.OPCODE[opActual];

                while (iActual < iExpected) {
                    assertTrue(itActual.hasNext(), "Reduced bytecode is smaller than expected version");
                    assertEquals(Mnemonic.OPCODE[Opcode.NOP], instrActual, "Overwritten instructions in the reduced version must be NOPs");
                    iActual = itActual.next();
                }

                // compare operations
                if (opExpected == Opcode.RETURN && opActual != opExpected)
                    assertEquals(Mnemonic.OPCODE[Opcode.NOP], instrActual, "A return instruction may only be overridden by a NOP");
                else
                    assertEquals(instrExpected, instrActual, () -> "Instructions at index " + iExpected + " have to be equal");
            }
            while (itActual.hasNext()) {
                var i      = itActual.next();
                var opcode = itActual.byteAt(i);

                assertEquals(Opcode.NOP, opcode, () -> "Instruction " + Mnemonic.OPCODE[opcode] + " that exceeds expected body must be NOPs");
            }
        } catch (BadBytecode e) {
            fail(e);
        }

        // ensure that both iterators have been exhausted
        assertFalse(itActual.hasNext());
    }
}
