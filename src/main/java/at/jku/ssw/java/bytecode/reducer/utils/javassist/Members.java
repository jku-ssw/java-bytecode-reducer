package at.jku.ssw.java.bytecode.reducer.utils.javassist;

import javassist.*;
import javassist.expr.MethodCall;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * Javassist utilities for members - those can either be (static) fields,
 * methods, (static) initializers or constructors.
 */
public final class Members {

    public enum Attribute {
        PUBLIC(Modifier.PUBLIC),
        PRIVATE(Modifier.PRIVATE),
        PROTECTED(Modifier.PROTECTED),
        STATIC(Modifier.STATIC),
        FINAL(Modifier.FINAL),
        SYNCHRONIZED(Modifier.SYNCHRONIZED),
        VOLATILE(Modifier.VOLATILE),
        VARARGS(Modifier.VARARGS),
        TRANSIENT(Modifier.TRANSIENT),
        NATIVE(Modifier.NATIVE),
        INTERFACE(Modifier.INTERFACE),
        ABSTRACT(Modifier.ABSTRACT),
        STRICT(Modifier.STRICT),
        ANNOTATION(Modifier.ANNOTATION),
        ENUM(Modifier.ENUM);

        public final int flag;

        Attribute(int flag) {
            this.flag = flag;
        }

        public static Stream<Attribute> randStream() {

            var values = Arrays.asList(values());

            // shuffle attributes to return them in random order but still
            // return every attribute only once
            Collections.shuffle(values);

            return values.stream();
        }
    }

    /**
     * Signature of the main method.
     */
    public static final String MAIN_SIGNATURE = "([Ljava/lang/String;)V";

    private Members() {
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
                member.getName().equals("main") &&
                MAIN_SIGNATURE.equals(member.getSignature());
    }

    /**
     * @see Members#isMain(CtMember)
     */
    public static boolean isNotMain(CtMember member) {
        return !isMain(member);
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
