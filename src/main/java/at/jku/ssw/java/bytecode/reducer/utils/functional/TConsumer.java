package at.jku.ssw.java.bytecode.reducer.utils.functional;

import java.util.function.Consumer;

/**
 * Wrapper for {@link Consumer} lambdas that allows exceptions to be thrown.
 * As those are wrapped into {@link RuntimeException},
 * it should only be used where exceptions indicate a non-recoverable failure.
 *
 * @param <T> The incoming parameter's type
 */
@FunctionalInterface
public interface TConsumer<T> extends Consumer<T> {
    @Override
    default void accept(T t) {
        try {
            acceptOrThrow(t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void acceptOrThrow(T t) throws Exception;
}
