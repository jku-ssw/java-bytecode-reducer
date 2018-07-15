package at.jku.ssw.java.bytecode.reducer.utils.functional;

/**
 * Wrapper for lambdas that allows exceptions to be thrown.
 * As those are wrapped into {@link RuntimeException},
 * it should only be used where exceptions indicate a non-recoverable failure.
 *
 * @param <T> The incoming parameter's type
 */
@FunctionalInterface
public interface TPredicate<T> {
    boolean test(T t) throws Exception;
}
