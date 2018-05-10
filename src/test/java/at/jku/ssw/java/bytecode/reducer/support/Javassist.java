package at.jku.ssw.java.bytecode.reducer.support;

import at.jku.ssw.java.bytecode.reducer.utils.TFunction;
import javassist.*;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.annotation.Annotation;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains static test utilities for Javassist.
 */
public interface Javassist {

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

        List<AttributeInfo> a1 = expected.getClassFile().getAttributes();
        List<AttributeInfo> a2 = actual.getClassFile().getAttributes();


        // compare attributes
        assertCollectionEquals(a1, a2,
                (AttributeInfo a, AttributeInfo b) -> a.getName().equals(b.getName()),
                (AttributeInfo a, AttributeInfo b) -> assertEquals(a.getName(), b.getName()));

        CtField[] f1 = expected.getDeclaredFields();
        CtField[] f2 = actual.getDeclaredFields();

        assertArrayEquals(f1, f2,
                (CtField a, CtField b) -> a.getName().equals(b.getName()),
                this::assertFieldEquals);

        CtBehavior[] b1 = expected.getDeclaredBehaviors();
        CtBehavior[] b2 = actual.getDeclaredBehaviors();

        assertArrayEquals(b1, b2,
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
     * @see Javassist#assertCollectionEquals(Collection, Collection, BiPredicate, BiConsumer)
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
    default void assertFieldEquals(CtField f1, CtField f2) {

        assertEquals(f1.getName(), f2.getName());

        try {
            assertEquals(f1.getType(), f2.getType());
        } catch (NotFoundException e) {
            fail(e);
        }

        assertEquals(f1.getConstantValue(), f2.getConstantValue());

        assertEquals(f1.getModifiers(), f2.getModifiers());

        List<AttributeInfo> a1 = f1.getFieldInfo().getAttributes();
        List<AttributeInfo> a2 = f1.getFieldInfo().getAttributes();

        assertCollectionEquals(a1, a2,
                (AttributeInfo a, AttributeInfo b) -> a.getName().equals(b.getName()),
                (AttributeInfo a, AttributeInfo b) -> assertEquals(a.getName(), b.getName()));

        assertAnnotationEquals(f1, f2,
                (TFunction<CtField, Object[]>) CtField::getAnnotations);
    }

    default void assertAnnotationEquals(Annotation a1, Annotation a2) {
        assertEquals(a1.getTypeName(), a2.getTypeName());

        assertCollectionEquals(a1.getMemberNames(), a2.getMemberNames(),
                String::equals,
                (a, b) -> assertEquals(a1.getMemberValue(a), a2.getMemberValue(b)));
    }

    private <T> void assertAnnotationEquals(T a,
                                            T b,
                                            Function<T, Object[]> prop) {

        assertArrayEquals(
                prop.apply(a),
                prop.apply(b),
                (Annotation a1, Annotation a2) -> a1.getTypeName().equals(a2.getTypeName()),
                this::assertAnnotationEquals
        );
    }

    default void assertBehaviourEquals(CtBehavior b1, CtBehavior b2) {
        if (b1 instanceof CtMethod && b2 instanceof CtMethod)
            assertMethodEquals((CtMethod) b1, (CtMethod) b2);
        if (b1 instanceof CtConstructor && b2 instanceof CtConstructor)
            assertConstructorEquals((CtConstructor) b1, (CtConstructor) b2);
    }

    default void assertMethodEquals(CtMethod m1, CtMethod m2) {
    }

    default void assertConstructorEquals(CtConstructor m1, CtConstructor m2) {
    }
}
