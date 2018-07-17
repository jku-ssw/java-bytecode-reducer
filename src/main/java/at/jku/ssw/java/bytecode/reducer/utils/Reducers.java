package at.jku.ssw.java.bytecode.reducer.utils;

import at.jku.ssw.java.bytecode.reducer.annot.Expensive;
import at.jku.ssw.java.bytecode.reducer.runtypes.Reducer;

import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Helper that contains static utilities to order, filter and fetch
 * modules.
 */
public class Reducers {
    private Reducers() {
    }

    public static <T extends Reducer> Stream<Class<? extends T>> sort(Stream<Class<? extends T>> modules) {
        return modules.sorted(Comparator.comparing(c -> c.isAnnotationPresent(Expensive.class) ? 1 : -1));
    }
}
