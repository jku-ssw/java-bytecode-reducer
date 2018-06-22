package at.jku.ssw.java.bytecode.reducer.utils.javassist;

import javassist.*;

/**
 * Javassist utilities for members - those can either be (static) fields,
 * methods, (static) initializers or constructors.
 *
 */
public final class Members {
    /**
     * Signature of the main method.
     */
    public static final String MAIN_SIGNATURE = "([Ljava/lang/String;)V";

    private Members() {}

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
}
