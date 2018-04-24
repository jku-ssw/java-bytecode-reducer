package at.jku.ssw.java.bytecode.reducer;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.annotation.Annotation;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Contains static test utilities for Javassist.
 */
public final class Javassist {
    public static boolean equals(CtClass c1, CtClass c2, CtClass c3, CtClass... others)
            throws NotFoundException {

        Iterator<CtClass> it = Stream.of(
                Collections.singletonList(c1),
                Collections.singletonList(c2),
                Collections.singletonList(c3),
                Arrays.asList(others)
        ).flatMap(Collection::stream)
                .iterator();

        CtClass a = it.next();
        CtClass b;
        while (it.hasNext()) {
            b = it.next();
            if (!equals(a, b))
                return false;
            a = b;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public static boolean equals(CtClass c1, CtClass c2) throws NotFoundException {
        if (c1 == null || c2 == null)
            return c1 == c2;

        // compare class names
        if (!c1.getName().equals(c2.getName()))
            return false;

        // compare extended classes
        if (!equals(c1.getDeclaringClass(), c2.getDeclaringClass()))
            return false;

        // compare inherited classes
        if (!equals(c1.getSuperclass(), c2.getSuperclass()))
            return false;

        BiPredicate<CtClass, CtClass> cmpClassNames = (a, b) -> a.getName().equals(b.getName());

        // compare inner / nested classes
        if (!equals(c1.getDeclaredClasses(), c2.getDeclaredClasses(), cmpClassNames))
            return false;

        // compare interfaces
        if (!equals(c1.getInterfaces(), c2.getInterfaces(), cmpClassNames))
            return false;

        List<AttributeInfo> a1 = c1.getClassFile().getAttributes();
        List<AttributeInfo> a2 = c2.getClassFile().getAttributes();

        BiPredicate<AttributeInfo, AttributeInfo> cmpAttributeNames = (a, b) -> a.getName().equals(b.getName());

        if (!compareAnnotations(c1, c2, c -> {
            try {
                return c.getAnnotations();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }))
            return false;


        // compare attributes
        if (!equals(a1, a2, cmpAttributeNames))
            return false;

        CtField[] f1 = c1.getDeclaredFields();
        CtField[] f2 = c2.getDeclaredFields();

        if (!equals(f1, f2, (CtField a, CtField b) -> a.getName().equals(b.getName()), Javassist::equals))
            return false;

        CtBehavior[] b1 = c1.getDeclaredBehaviors();
        CtBehavior[] b2 = c2.getDeclaredBehaviors();

        // TODO

        return true;
    }

    /**
     * Utility methods to compare member lists (e.g. classes, fields).
     * This comparison is only shallow as the only modified object
     * is usually the declaring class / member.
     *
     * @param a1         The first component's attributes
     * @param a2         The second component's attributes
     * @param filter     Function to filter the applicable results for
     *                   the comparator
     * @param comparator Function to determine whether an attribute of
     *                   one component is also an attribute of the other
     * @param <T>        The type of the compared attributes
     * @return {@code true} if every attribute of the first component
     * also occurs in the second component's attribute list
     */
    @SuppressWarnings("unchecked")
    private static <T> boolean equals(Object[] a1,
                                      Object[] a2,
                                      BiPredicate<T, T> filter,
                                      BiPredicate<T, T> comparator) {

        return equals(List.of(a1), List.of(a2), filter, comparator);
    }

    @SuppressWarnings("unchecked")
    private static <T> boolean equals(Collection l1,
                                      Collection l2,
                                      BiPredicate<T, T> filter,
                                      BiPredicate<T, T> comparator) {

        return ((List<T>) l1).size() == ((List<T>) l2).size() &&
                ((List<T>) l1).stream().allMatch(a ->
                        comparator.test(
                                a,
                                ((List<T>) l2).stream()
                                        .filter(b -> filter.test(a, b))
                                        .findAny()
                                        .orElse(null)
                        )
                );
    }

    private static <T> boolean equals(Collection l1,
                                      Collection l2,
                                      BiPredicate<T, T> comparator) {
        return equals(l1, l2, comparator, comparator);
    }

    private static <T> boolean equals(Object[] a1,
                                      Object[] a2,
                                      BiPredicate<T, T> comparator) {
        return equals(a1, a2, comparator, comparator);
    }

    @SuppressWarnings("unchecked")
    public static boolean equals(CtField f1, CtField f2) {

        if (!f1.getName().equals(f2.getName()))
            return false;

        try {
            if (!f1.getType().equals(f2.getType()))
                return false;
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

        if (!f1.getConstantValue().equals(f2.getConstantValue()))
            return false;

        if (f1.getModifiers() != f2.getModifiers())
            return false;

        List<AttributeInfo> a1 = f1.getFieldInfo().getAttributes();
        List<AttributeInfo> a2 = f1.getFieldInfo().getAttributes();

        if (!equals(a1, a2, (AttributeInfo a, AttributeInfo b) -> a.getName().equals(b.getName())))
            return false;

        return compareAnnotations(f1, f2, f -> {
            try {
                return f.getAnnotations();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public static boolean equals(Annotation a1, Annotation a2) {
        if (!a1.getTypeName().equals(a2.getTypeName()))
            return false;

        BiPredicate<String, String> cmpMemberValues = (a, b) -> a1.getMemberValue(a).equals(a2.getMemberValue(b));

        return equals(a1.getMemberNames(), a2.getMemberNames(), String::equals, cmpMemberValues);
    }


    private static <T> boolean compareAnnotations(T a,
                                                  T b,
                                                  Function<T, Object[]> prop) {
        BiPredicate<Annotation, Annotation> cmpAnnotationNames = (a1, a2) ->
                a1.getTypeName().equals(a2.getTypeName());

        return equals(
                prop.apply(a),
                prop.apply(b),
                cmpAnnotationNames,
                Javassist::equals
        );
    }
}
