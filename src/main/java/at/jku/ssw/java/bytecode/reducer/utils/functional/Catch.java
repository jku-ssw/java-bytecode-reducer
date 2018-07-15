package at.jku.ssw.java.bytecode.reducer.utils.functional;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Provides functional helpers to wrap throwing blocks into Java 8 lambdas.
 * Errors that occur in those cases are then wrapped in
 * {@link RuntimeException}s.
 */
public final class Catch {
    private Catch() {
    }

    public static <T> Predicate<T> predicate(TPredicate<T> predicate) {
        return t -> {
            try {
                return predicate.test(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <A, B> Function<A, B> function(TFunction<A, B> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T> Consumer<T> consumer(TConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
