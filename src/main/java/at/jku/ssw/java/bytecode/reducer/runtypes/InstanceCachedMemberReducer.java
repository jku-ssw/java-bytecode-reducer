package at.jku.ssw.java.bytecode.reducer.runtypes;

/**
 * Interface that provides methods to reduce or apply reduction operations
 * on class members, where the concrete type of member can be specified
 * by the implementing class.
 * Here each member instance is directly cached.
 *
 * @param <CLASS>  The type for types
 * @param <MEMBER> The type for members
 *                 (e.g. implementations for fields, methods)
 */
public interface InstanceCachedMemberReducer<CLASS, MEMBER>
        extends MemberReducer<CLASS, MEMBER, MEMBER> {

    @Override
    default MEMBER keyFromMember(MEMBER member) {
        return member;
    }

}
