package at.jku.ssw.java.bytecode.reducer.utils;

import java.util.Objects;

/**
 * Combines an attribute with its corresponding member
 * (e.g. a {@code transient} field).
 */
public class MemberAttribute {

    /**
     * String that uniquely identifies this member (e.g. descriptor, name).
     */
    public final String member;

    /**
     * Integer that identifies the attribute.
     */
    public final int attribute;

    public MemberAttribute(String member, int attribute) {
        this.member = member;
        this.attribute = attribute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberAttribute that = (MemberAttribute) o;
        return attribute == that.attribute &&
                Objects.equals(member, that.member);
    }

    @Override
    public int hashCode() {
        return Objects.hash(member, attribute);
    }
}
