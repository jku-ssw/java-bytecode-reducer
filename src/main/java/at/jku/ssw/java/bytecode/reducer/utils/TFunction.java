package at.jku.ssw.java.bytecode.reducer.utils;

import java.util.function.Function;

/**
 * Wrapper for {@link Function} lambdas that allows exceptions to be thrown.
 * As those are wrapped into {@link RuntimeException},
 * it should only be used where exceptions indicate a non-recoverable failure.
 *
 * @param <T> The incoming parameter's type
 * @param <R> The resulting type
 */
@FunctionalInterface
public interface TFunction<T, R> extends Function<T, R> {
    @Override
    default R apply(T t) {
        try {
            return applyOrThrow(t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    R applyOrThrow(T t) throws Exception;
}
