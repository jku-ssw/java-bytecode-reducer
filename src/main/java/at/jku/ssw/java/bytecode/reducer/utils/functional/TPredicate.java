package at.jku.ssw.java.bytecode.reducer.utils.functional;

import java.util.function.Predicate;

/**
 * Wrapper for {@link Predicate} lambdas that allows exceptions to be thrown.
 * As those are wrapped into {@link RuntimeException},
 * it should only be used where exceptions indicate a non-recoverable failure.
 *
 * @param <T> The incoming parameter's type
 */
@FunctionalInterface
public interface TPredicate<T> extends Predicate<T> {
    @Override
    default boolean test(T t) {
        try {
            return testOrThrow(t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    boolean testOrThrow(T t) throws Exception;
}
