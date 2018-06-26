package at.jku.ssw.java.bytecode.reducer.utils;

/**
 * Identifies a code range within a given member.
 */
public class CodePosition {
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

    public CodePosition(String member, int begin, int end) {
        this.member = member;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CodePosition that = (CodePosition) o;

        if (begin != that.begin) return false;
        if (end != that.end) return false;
        return member.equals(that.member);
    }

    @Override
    public int hashCode() {
        int result = member.hashCode();
        result = 31 * result + begin;
        result = 31 * result + end;
        return result;
    }
}
