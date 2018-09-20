package at.jku.ssw.java.bytecode.reducer.runtypes;

import at.jku.ssw.java.bytecode.reducer.context.Reduction.Base;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Result;

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
     *                 bytecode is valid
     * @return the minimal bytecode
     * @throws Exception if the bytecode access at some point reports errors
     */
    @Override
    default byte[] apply(byte[] bytecode, Predicate<byte[]> test) throws Exception {
        Base<A>   base = Base.of(bytecode);
        Result<A> res;
        byte[]    reduced;

        do {
            res = apply(base);
            reduced = res.bytecode();

            base = test.test(reduced) ? res.accept() : res.reject();
        } while (!res.isMinimal());

        return reduced;
    }

    @Override
    default byte[] apply(byte[] bytecode) throws Exception {
        // the default implementation of the reducer
        // without providing a check simply forces a result
        return apply(bytecode, __ -> true);
    }
}
