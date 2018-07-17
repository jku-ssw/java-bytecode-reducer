package at.jku.ssw.java.bytecode.reducer.runtypes;

import at.jku.ssw.java.bytecode.reducer.context.Reduction.Base;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Result;
import at.jku.ssw.java.bytecode.reducer.utils.functional.Catch;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Interface that provides methods to reduce or apply reduction operations
 * on class members, where the concrete type of member can be specified
 * by the implementing class.
 * Already tried members are stored in a cache where each member is bound
 * to a specific key by a given mapping.
 *
 * @param <CLASS>  The type for types
 * @param <MEMBER> The type for members
 *                 (e.g. implementations for fields, methods)
 * @param <CACHE>  The type of values to cache
 */
public interface MemberReducer<CLASS, MEMBER, CACHE>
        extends ForcibleReducer<CACHE>, BytecodeTransformer<CLASS> {

    @Override
    default Result<CACHE> apply(Base<CACHE> base) throws Exception {

        CLASS clazz = classFrom(base.bytecode());

        // get the first applicable member that was not already attempted
        Optional<MEMBER> optMember = getMembers(clazz)
                .filter(m -> base.isNotCached(keyFromMember(m)))
                .findAny();

        // if no applicable member was found, the reduction is minimal
        return optMember.map(Catch.function(m ->
                base.toResult(bytecodeFrom(process(clazz, m)), keyFromMember(m))))
                .orElseGet(base::toMinimalResult);
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

    /**
     * Retrieves the key from the given member instance.
     *
     * @param member The member to generate the key for
     * @return the key that uniquely identifies this member object in the cache
     */
    CACHE keyFromMember(MEMBER member);
}
