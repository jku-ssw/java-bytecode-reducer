package at.jku.ssw.java.bytecode.reducer.runtypes;

import at.jku.ssw.java.bytecode.reducer.context.Reduction;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Base;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Result;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TFunction;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Interface that provides methods to reduce or apply reduction operations
 * on class members, where the concrete type of member can be specified
 * by the implementing class.
 *
 * @param <CLASS>  The type for types
 * @param <MEMBER> The type for members
 *                 (e.g. implementations for fields, methods)
 */
public interface MemberReducer<CLASS, MEMBER>
        extends RepeatableReducer<MEMBER>, BytecodeTransformer<CLASS> {

    @Override
    default Result<MEMBER> apply(Base<MEMBER> base) throws Exception {

        CLASS clazz = classFrom(base.bytecode());

        // get the first applicable member that was not already attempted
        Optional<MEMBER> optMember = getMembers(clazz)
                .filter(f -> !base.cache().contains(f))
                .findAny();

        // if no applicable member was found, the reduction is minimal
        return optMember.map((TFunction<MEMBER, Result<MEMBER>>) f ->
                base.toResult(bytecodeFrom(process(clazz, f)), f))
                .orElseGet(base::toMinimalResult);
    }

    @Override
    default Result<MEMBER> force(byte[] bytecode) throws Exception {
        CLASS clazz = classFrom(bytecode);

        Base<MEMBER> base = Reduction.of(
                bytecodeFrom(
                        getMembers(clazz)
                                .map(f -> (TFunction<CLASS, CLASS>) c -> process(c, f))
                                .reduce(c -> c, (f1, f2) -> c -> f2.apply(f1.apply(c)))
                                .apply(clazz)));

        return base.toMinimalResult();
    }

    /**
     * Retrieves potentially applicable members that should be attempted.
     *
     * @param clazz The class type instance
     * @return a stream of potential members (of the given member type)
     * @throws Exception if the members cannot be extracted / identified
     */
    Stream<MEMBER> getMembers(CLASS clazz) throws Exception;

    /**
     * Processes the given member of the class.
     *
     * @param clazz  The class that this member belongs to
     * @param member The member instance that should be processed
     * @return the processed class instance
     * @throws Exception if the processing failed
     */
    CLASS process(CLASS clazz, MEMBER member) throws Exception;
}
