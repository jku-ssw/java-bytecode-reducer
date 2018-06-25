package at.jku.ssw.java.bytecode.reducer.utils;

import javassist.bytecode.MethodInfo;

public class CodePosition {
    public final MethodInfo method;
    public final int        begin;
    public final int        end;

    public CodePosition(MethodInfo method, int begin, int end) {
        this.method = method;
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
        return method.equals(that.method);
    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + begin;
        result = 31 * result + end;
        return result;
    }
}
