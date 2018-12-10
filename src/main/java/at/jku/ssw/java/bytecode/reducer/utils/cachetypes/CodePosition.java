package at.jku.ssw.java.bytecode.reducer.utils.cachetypes;

/**
 * Identifies a code range within a given member.
 */
public class CodePosition implements Comparable<CodePosition> {
    /**
     * Unique name of the corresponding member (e.g. descriptor, name).
     */
    public final String member;

    /**
     * Start of the code range.
     */
    public final int begin;

    /**
     * End of the code range.
     */
    public final int end;

    /**
     * Creates a new code position for the given index range.
     *
     * @param member The corresponding member name
     * @param begin  The start of the index range
     * @param end    The end of the index range
     */
    public CodePosition(String member, int begin, int end) {
        assert member != null;
        assert end >= begin;
        assert begin >= 0;
        assert end >= 0;

        this.member = member;
        this.begin = begin;
        this.end = end;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CodePosition that = (CodePosition) o;

        if (begin != that.begin) return false;
        if (end != that.end) return false;
        return member.equals(that.member);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = member.hashCode();
        result = 31 * result + begin;
        result = 31 * result + end;
        return result;
    }

    /**
     * Compares the given code positions using the lexicographic ordering
     * of the range as well as the corresponding member.
     *
     * @param codePosition The other code position that is compared
     * @return a value below {@code 0} if this code position's order is after
     * the given one, a value above {code 0} if it is before the given code
     * position; {@code 0} if both are equal
     */
    @Override
    public int compareTo(CodePosition codePosition) {
        if (!member.equals(codePosition.member))
            return codePosition.member.compareTo(member);

        if (end != codePosition.end)
            return codePosition.end - end;

        if (begin != codePosition.begin)
            return codePosition.begin - begin;

        return 0;
    }
}
