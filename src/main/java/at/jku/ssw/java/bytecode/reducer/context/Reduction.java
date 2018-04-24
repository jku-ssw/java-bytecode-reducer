package at.jku.ssw.java.bytecode.reducer.context;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents either a reduction {@link Base} or {@link Result} that
 * may be the argument or result of a
 * {@link at.jku.ssw.java.bytecode.reducer.runtypes.Reducer}.
 *
 * @param <T> The type of the attempt cache
 */
public abstract class Reduction<T> {

    /**
     * The current byte code.
     */
    protected final byte[] bytecode;

    /**
     * The attempts of previous reducers.
     */
    protected final Set<T> cache;

    /**
     * Increasing identifier for consecutive runs.
     */
    protected final int run;

    /**
     * Instantiate a new object with the given byte code and attempt cache.
     *
     * @param bytecode The byte code that represents this base / result
     * @param cache    The initial cache (empty if null)
     * @param run      The run number (default 0)
     */
    private Reduction(byte[] bytecode, Set<T> cache, int run) {
        this.bytecode = bytecode;
        this.cache = cache;
        this.run = run;
    }

    /**
     * @see Reduction#Reduction(byte[], Set, int)
     */
    private Reduction(byte[] bytecode, int run) {
        this(bytecode, Set.of(), run);
    }

    /**
     * Instantiate a base from the given byte code
     *
     * @param bytecode The byte code describing the class
     * @param <U>      The type of the the cached attempts
     * @return a new reduction base
     */
    public static <U> Base<U> of(byte[] bytecode) {
        return new Base<>(Arrays.copyOf(bytecode, bytecode.length));
    }

    /**
     * Returns the byte code.
     *
     * @return a byte array describing a class
     */
    public final byte[] bytecode() {
        return Arrays.copyOf(bytecode, bytecode.length);
    }

    /**
     * Returns the attempt cache.
     *
     * @return a set of attempts
     */
    public final Set<T> cache() {
        return cache;
    }

    /**
     * Represents the base of a reduction.
     * Stores the source byte code and any previous attempt.
     *
     * @param <T> The type of the stored attempts
     */
    public static final class Base<T> extends Reduction<T> {

        /**
         * @see Reduction#Reduction(byte[], Set, int)
         */
        private Base(byte[] bytecode, Set<T> cache, int run) {
            super(bytecode, cache, run);
        }

        /**
         * @see Reduction#Reduction(byte[], Set, int)
         */
        private Base(byte[] bytecode, int run) {
            super(bytecode, run);
        }

        /**
         * @see Reduction#Reduction(byte[], Set, int)
         */
        private Base(byte[] bytecode) {
            super(bytecode, 0);
        }

        /**
         * Transforms the base into a new {@link Result}
         * that stores this base's byte code and the result of a
         * {@link at.jku.ssw.java.bytecode.reducer.runtypes.Reducer}.
         * Also appends the new attempts.
         *
         * @param bytecode The reduced byte code (probably invalid)
         * @param attempts The updates that led to this result
         * @return a new result that stores the current and reduced byte codes
         */
        public final Result<T> toResult(byte[] bytecode, Set<T> attempts) {
            return new Result<>(this, bytecode, attempts);
        }

        /**
         * @see Base#toResult(byte[], Set)
         */
        @SafeVarargs
        public final Result<T> toResult(byte[] bytecode, T... attempts) {
            return new Result<>(this, bytecode, Set.of(attempts));
        }

        /**
         * Transforms the base into a new {@link Result}
         * that stores this base's byte code and the result of a
         * {@link at.jku.ssw.java.bytecode.reducer.runtypes.Reducer}.
         * Also appends the new attempts.
         *
         * @return a new result that stores the current and reduced byte codes
         */
        public final Result<T> toMinimalResult() {
            return new Result<>(this, bytecode, Set.of(), true);
        }
    }

    /**
     * Represents the result of a reduction.
     * Stores the source and transformed byte codes and the attempt cache.
     *
     * @param <T> The type of the stored attempts
     */
    public static final class Result<T> extends Reduction<T> {

        /**
         * The byte code that produced this result.
         * Is stored in order to revert back to this in
         * case of invalid results.
         */
        private final byte[] previous;

        /**
         * Flag that indicates that a result is minimal.
         */
        protected final boolean minimal;

        /**
         * Create a new result based on the given {@link Base}
         * in combination with the new byte code and additional attempts.
         *
         * @param base     The base that produced this result
         * @param bytecode The resulting byte code
         * @param attempts The additional updates
         * @param min      Indicates whether the result is minimal
         */
        private Result(Base<T> base, byte[] bytecode, Set<T> attempts, boolean min) {
            super(bytecode, Stream.of(base.cache, attempts)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet()), base.run + 1);
            this.previous = base.bytecode;
            this.minimal = min;
        }

        /**
         * Creates a new (non-minimal) result based on the given {@link Base}
         * in combination with the new byte code and additional attempts
         *
         * @param base     The base that produced this result
         * @param bytecode The resulting byte code
         * @param attempts The additional updates
         */
        private Result(Base<T> base, byte[] bytecode, Set<T> attempts) {
            this(base, bytecode, attempts, false);
        }

        /**
         * Accepts the given byte code changes as a new valid base
         * and resets the attempt cache.
         *
         * @return a new reduction base with the new byte code and the default
         * attempt log
         */
        public Base<T> accept() {
            return new Base<>(bytecode, run);
        }

        /**
         * Rejects the given byte code changes and returns a new base
         * consisting of the previous byte code and the cached attempts.
         *
         * @return a new reduction base consisting of the source byte code
         * and the cached attempts
         */
        public Base<T> reject() {
            return new Base<>(previous, cache, run);
        }

        /**
         * Indicates whether the result is minimal.
         *
         * @return {@code true} if the result is minimal;
         * {@code false} otherwise
         */
        public boolean isMinimal() {
            return minimal;
        }
    }
}
