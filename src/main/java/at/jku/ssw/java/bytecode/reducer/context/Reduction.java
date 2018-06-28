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
     * The current bytecode.
     */
    protected final byte[] bytecode;

    /**
     * The attempts of previous reducers.
     */
    protected final Set<T> cache;

    /**
     * Increasing identifier for consecutive runs.
     */
    public final int run;

    /**
     * Instantiate a new object with the given bytecode and attempt cache.
     *
     * @param bytecode The bytecode that represents this base / result
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
     * Instantiate a base from the given bytecode
     *
     * @param bytecode The bytecode describing the class
     * @param <U>      The type of the the cached attempts
     * @return a new reduction base
     */
    public static <U> Base<U> of(byte[] bytecode) {
        return new Base<>(Arrays.copyOf(bytecode, bytecode.length));
    }

    /**
     * Returns the bytecode.
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
     * Stores the source bytecode and any previous attempt.
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
         * that stores this base's bytecode and the result of a
         * {@link at.jku.ssw.java.bytecode.reducer.runtypes.Reducer}.
         * Also appends the new attempts.
         *
         * @param bytecode The reduced bytecode (probably invalid)
         * @param attempts The updates that led to this result
         * @return a new result that stores the current and reduced bytecodes
         */
        @SafeVarargs
        public final Result<T> toResult(byte[] bytecode, T... attempts) {
            return new Result<>(this, bytecode, Set.of(attempts));
        }

        /**
         * Transforms the base into a new {@link Result}
         * that stores this base's bytecode and the result of a
         * {@link at.jku.ssw.java.bytecode.reducer.runtypes.Reducer}.
         * Also appends the new attempts.
         *
         * @return a new result that stores the current and reduced bytecodes
         */
        public final Result<T> toMinimalResult() {
            return new Result<>(this, bytecode, Set.of(), true);
        }
    }

    /**
     * Represents the result of a reduction.
     * Stores the source and transformed bytecodes and the attempt cache.
     *
     * @param <T> The type of the stored attempts
     */
    public static final class Result<T> extends Reduction<T> {

        /**
         * The bytecode that produced this result.
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
         * in combination with the new bytecode and additional attempts.
         *
         * @param base     The base that produced this result
         * @param bytecode The resulting bytecode
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
         * in combination with the new bytecode and additional attempts
         *
         * @param base     The base that produced this result
         * @param bytecode The resulting bytecode
         * @param attempts The additional updates
         */
        private Result(Base<T> base, byte[] bytecode, Set<T> attempts) {
            this(base, bytecode, attempts, false);
        }

        /**
         * Accepts the given bytecode changes as a new valid base
         * and resets the attempt cache.
         *
         * @return a new reduction base with the new bytecode and the default
         * attempt log
         */
        public Base<T> accept() {
            return new Base<>(bytecode, run);
        }

        /**
         * Rejects the given bytecode changes and returns a new base
         * consisting of the previous bytecode and the cached attempts.
         *
         * @return a new reduction base consisting of the source bytecode
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
