package at.jku.ssw.java.bytecode.reducer.runtypes;

import at.jku.ssw.java.bytecode.reducer.states.Reduction.Base;
import at.jku.ssw.java.bytecode.reducer.states.Reduction.Result;

import java.util.function.Predicate;

/**
 * Represents a reduction of a class file that also maintains a certain
 * state that can be passed to the next iteration.
 *
 * @param <A> The type of the attempt log
 */
public interface IterativeReducer<A> extends Reducer {

    /**
     * Apply the transformation on the given base and return a
     * contextual result.
     *
     * @param base The base that contains the bytecode and attempt log
     * @return the new bytecode and attempted objects in a
     * {@link Result} object
     * @throws Exception if the bytecode is invalid
     */
    Result<A> apply(Base<A> base) throws Exception;

    /**
     * Applies the reduction operation until a minimal result is found.
     * Success or failure of a particular reduction is tested via the
     * given {@link Predicate}.
     *
     * @param bytecode The bytecode to reduce
     * @param test     The function that determines whether the resulting
     *                 bytecode is interesting
     * @return the minimal bytecode
     * @throws Exception if the bytecode access at some point reports errors
     */
    @Override
    default byte[] apply(byte[] bytecode, Predicate<byte[]> test) throws Exception {
        return iterate(Base.of(bytecode), test);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default byte[] apply(byte[] bytecode) throws Exception {
        // the default implementation of the reducer
        // without providing a check simply forces a result
        return apply(bytecode, __ -> true);
    }

    /**
     * Applies this transformation iteratively to the given base
     * until a minimal result is produced.
     *
     * @param base The base that contains the bytecode to reduce
     * @param test The function that determines whether the resulting bytecode
     *             is interesting
     * @return the minimal bytecode
     * @throws Exception if the bytecode access fails
     */
    default byte[] iterate(Base<A> base, Predicate<byte[]> test) throws Exception {
        Result<A> res;
        byte[] reduced;

        for (; ; ) {
            res = apply(base);
            reduced = res.bytecode();

            // assumption that a minimal result was already checked
            if (res.isMinimal())
                return reduced;

            base = test.test(reduced) ? res.accept() : res.reject();
        }
    }
}
