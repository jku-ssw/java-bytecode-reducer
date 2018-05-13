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
public interface RepeatableReducer<A> extends Reducer {

    /**
     * Apply the transformation on the given base and return a
     * contextual result.
     *
     * @param base The base that contains the byte code and attempt log
     * @return the new byte code and attempted objects in a
     * {@link Result} object
     * @throws Exception if the byte code is invalid
     */
    Result<A> apply(Base<A> base) throws Exception;

    /**
     * Applies the reduction operation until a minimal result is found.
     * Success or failure of a particular reduction is tested via the
     * given {@link Predicate}.
     *
     * @param bytecode The byte code to reduce
     * @param test     The function that determines whether the resulting
     *                 byte code is valid
     * @return the minimal byte code
     * @throws Exception if the byte code access at some point reports errors
     */
    default byte[] getMinimal(byte[] bytecode, Predicate<Result<A>> test) throws Exception {
        Result<A> res = force(bytecode);

        // try forced result (assumed to be minimal)
        if (test.test(res))
            return res.bytecode();

        Base<A> base = res.reject();

        // otherwise try iterative approach
        do {
            res = apply(base);

            base = test.test(res) ? res.accept() : res.reject();
        } while (!res.isMinimal());

        return res.bytecode();
    }

    /**
     * Forces a minimal version of the reduction.
     * This may fail and require an iterative approach (with attempt log)
     *
     * @param bytecode The byte code
     * @return a new {@link Result} containing the byte code and attempt log
     * @throws Exception if the byte code is invalid
     */
    Result<A> force(byte[] bytecode) throws Exception;

    @Override
    default byte[] apply(byte[] bytecode) throws Exception {
        return force(bytecode).bytecode();
    }
}
