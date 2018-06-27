package at.jku.ssw.java.bytecode.reducer.runtypes;

public interface AssignmentReplacer {
    String PATTERN = "$_ = ";

    default String replaceWith(String value) {
        return "{ " + PATTERN + value + "; }";
    }
}
