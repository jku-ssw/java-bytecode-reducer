package at.jku.ssw.java.bytecode.reducer.runtypes;

import at.jku.ssw.java.bytecode.reducer.states.State;
import at.jku.ssw.java.bytecode.reducer.states.State.Experimental;

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
        var res = force(bytecode);
        var reduced = res.bytecode();

        // try forced result (assumed to be minimal)
        if (test.test(reduced))
            return reduced;

        var base = res.reject();

        // otherwise try iterative approach
        return iterate(base, test);
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
     * @return a new {@link Experimental} containing the bytecode and attempt log
     * @throws Exception if the bytecode is invalid
     */
    default Experimental<A> force(byte[] bytecode) throws Exception {
        State.Stable<A> stable = State.of(bytecode);
        Experimental<A> res;

        do {
            // apply the reduction operation
            res = apply(stable);

            // and "accept" the result / assume it to be correct
            stable = res.accept();

            // only return on a minimal result
        } while (!res.isMinimal());

        // create a new result that has the "unforced" bytecode
        // as a previous result
        return State.<A>of(bytecode).toResult(res.bytecode());
    }
}
