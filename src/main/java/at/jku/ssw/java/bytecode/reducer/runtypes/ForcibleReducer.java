package at.jku.ssw.java.bytecode.reducer.runtypes;

import at.jku.ssw.java.bytecode.reducer.context.Reduction;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Result;

import java.util.function.Predicate;

/**
 * Represents a repeatable reducer that also allows forcing a result
 * by applying all possible reductions without validating each step
 * (e.g. removing all applicable fields).
 *
 * @param <A> The type of the attempt log
 */
public interface ForcibleReducer<A> extends IterativeReducer<A> {

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
        var res     = force(bytecode);
        var reduced = res.bytecode();

        // try forced result (assumed to be minimal)
        if (test.test(reduced))
            return reduced;

        var base = res.reject();

        // otherwise try iterative approach
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
        return force(bytecode).bytecode();
    }

    /**
     * Forces a minimal version of the reduction.
     * This may fail and require an iterative approach (with attempt log)
     *
     * @param bytecode The bytecode
     * @return a new {@link Result} containing the bytecode and attempt log
     * @throws Exception if the bytecode is invalid
     */
    default Result<A> force(byte[] bytecode) throws Exception {
        Reduction.Base<A> base = Reduction.of(bytecode);
        Result<A>         res;

        do {
            // apply the reduction operation
            res = apply(base);

            // and "accept" the result / assume it to be correct
            base = res.accept();

            // only return on a minimal result
        } while (!res.isMinimal());

        // create a new reduction result that has the "unforced" bytecode
        // as a previous result
        return Reduction.<A>of(bytecode).toResult(res.bytecode());
    }
}
