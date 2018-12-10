package at.jku.ssw.java.bytecode.reducer.states;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents either a reduction {@link Stable} or {@link Experimental} that
 * may be the argument or result of a
 * {@link at.jku.ssw.java.bytecode.reducer.runtypes.Reducer}.
 *
 * @param <T> The type of the attempt cache
 */
public abstract class State<T> {

    /**
     * The current bytecode.
     */
    protected final byte[] bytecode;

    /**
     * The attempts of previous reducers.
     */
    protected final Set<T> attempts;

    /**
     * Increasing identifier for consecutive runs.
     */
    public final int run;

    /**
     * Instantiate a new object with the given bytecode and attempt cache.
     *
     * @param bytecode The bytecode that represents this base / result
     * @param attempts The initial cache (empty if null)
     * @param run      The run number (default 0)
     */
    protected State(byte[] bytecode, Set<T> attempts, int run) {
        this.bytecode = bytecode;
        this.attempts = attempts;
        this.run = run;
    }

    /**
     * @see State#State(byte[], Set, int)
     */
    protected State(byte[] bytecode, int run) {
        this(bytecode, Set.of(), run);
    }

    /**
     * Instantiate a base from the given bytecode
     *
     * @param bytecode The bytecode describing the class
     * @param <U>      The type of the cached attempts
     * @return a new reduction base
     */
    public static <U> Stable<U> of(byte[] bytecode) {
        return new Stable<>(Arrays.copyOf(bytecode, bytecode.length));
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
     * Determines whether the given value is already cached (was already
     * tried with this result).
     *
     * @param attempt The current attempt
     * @return {@code true} if this attempt was already tried, {@code false}
     * otherwise
     */
    public final boolean isCached(T attempt) {
        return attempts.contains(attempt);
    }

    /**
     * @see State#isCached(Object)
     */
    public final boolean isNotCached(T attempt) {
        return !isCached(attempt);
    }

    /**
     * Represents the base of a reduction.
     * Stores the source bytecode and any previous attempt.
     *
     * @param <T> The type of the stored attempts
     */
    public static class Stable<T> extends State<T> {

        /**
         * @see State#State(byte[], Set, int)
         */
        protected Stable(byte[] bytecode, Set<T> cache, int run) {
            super(bytecode, cache, run);
        }

        /**
         * @see State#State(byte[], Set, int)
         */
        protected Stable(byte[] bytecode, int run) {
            super(bytecode, run);
        }

        /**
         * @see State#State(byte[], Set, int)
         */
        protected Stable(byte[] bytecode) {
            super(bytecode, 0);
        }

        /**
         * Transforms the base into a new {@link Experimental}
         * that stores this base's bytecode and the result of a
         * {@link at.jku.ssw.java.bytecode.reducer.runtypes.Reducer}.
         * Also appends the new attempts.
         *
         * @param bytecode The reduced bytecode (probably invalid)
         * @param attempts The updates that led to this result
         * @return a new result that stores the current and reduced bytecodes
         */
        @SafeVarargs
        public final Experimental<T> toResult(byte[] bytecode, T... attempts) {
            if (Arrays.equals(this.bytecode, bytecode))
                return new Experimental.Failure<>(this, bytecode, Set.of(attempts));
            else
                return new Experimental<>(this, bytecode, Set.of(attempts));
        }

        /**
         * Transforms the base into a new {@link Experimental}
         * that stores this base's bytecode and the result of a
         * {@link at.jku.ssw.java.bytecode.reducer.runtypes.Reducer}.
         * Also appends the new attempts.
         *
         * @return a new result that stores the current and reduced bytecodes
         */
        public final Experimental<T> toMinimalResult() {
            return new Experimental<>(this, bytecode, Set.of(), true);
        }
    }

    /**
     * Represents the result of a reduction.
     * Stores the source and transformed bytecodes and the attempt cache.
     *
     * @param <T> The type of the stored attempts
     */
    public static class Experimental<T> extends State<T> {

        private static class Failure<T> extends Experimental<T> {

            protected Failure(Stable<T> stable, byte[] bytecode, Set<T> attempts) {
                super(stable, bytecode, attempts);
            }

            @Override
            public Stable<T> accept() {
                return super.reject();
            }
        }

        /**
         * The bytecode that produced this result.
         * Is stored in order to revert back to this in
         * case of invalid results.
         */
        protected final byte[] previous;

        /**
         * Flag that indicates that a result is minimal.
         */
        protected final boolean minimal;

        /**
         * Create a new result based on the given {@link Stable}
         * in combination with the new bytecode and additional attempts.
         *
         * @param stable   The base that produced this result
         * @param bytecode The resulting bytecode
         * @param attempts The additional updates
         * @param min      Indicates whether the result is minimal
         */
        protected Experimental(Stable<T> stable, byte[] bytecode, Set<T> attempts, boolean min) {
            super(bytecode, Stream.of(stable.attempts, attempts)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet()), stable.run + 1);
            this.previous = stable.bytecode;
            this.minimal = min;
        }

        /**
         * Creates a new (non-minimal) result based on the given {@link Stable}
         * in combination with the new bytecode and additional attempts
         *
         * @param stable   The base that produced this result
         * @param bytecode The resulting bytecode
         * @param attempts The additional updates
         */
        protected Experimental(Stable<T> stable, byte[] bytecode, Set<T> attempts) {
            this(stable, bytecode, attempts, false);
        }

        /**
         * Accepts the given bytecode changes as a new valid base
         * and resets the attempt cache.
         *
         * @return a new reduction base with the new bytecode and the default
         * attempt log
         */
        public Stable<T> accept() {
            return new Stable<>(bytecode, run);
        }

        /**
         * Rejects the given bytecode changes and returns a new base
         * consisting of the previous bytecode and the cached attempts.
         *
         * @return a new reduction base consisting of the source bytecode
         * and the cached attempts
         */
        public Stable<T> reject() {
            return new Stable<>(previous, attempts, run);
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
