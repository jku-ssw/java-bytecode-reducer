package at.jku.ssw.java.bytecode.reducer.io.files;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Naming generation conventions for temporary directories.
 * Exposes the {@link FunctionalInterface} method {@link #generate()}
 * and also provides default implementations for iteration over
 * generated names (probably endless).
 */
@FunctionalInterface
public interface NamingStrategy extends Iterable<String> {

    int MAX_ATTEMPTS = 10;

    String PREFIX    = ".tmp";
    String SEPARATOR = "-";

    /**
     * Generate a new name and return it.
     *
     * @return a string that depends on the implemented strategy
     */
    String generate();

    @Override
    default Iterator<String> iterator() {
        return new Iterator<>() {

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public String next() {
                return generate();
            }
        };
    }

    /**
     * Stream the generated names.
     * Unless the name generator is static, this represents an infinite stream.
     *
     * @return a {@link Stream} of strings
     */
    default Stream<String> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    /**
     * Creates a new {@link NamingStrategy} that prepends the instance's
     * {@link Object#toString()} value to a randomly generated numeric string.
     *
     * @param obj The object whose identifier is prefixed
     * @param <T> The generic type of the object
     * @return a naming strategy that generates a name
     * based on the given instance
     */
    static <T> NamingStrategy ForInstance(T obj) {
        final String partial = PREFIX + SEPARATOR + obj.getClass().getSimpleName() + SEPARATOR;

        return () -> partial + randPostfix();
    }

    /**
     * Creates a static {@link NamingStrategy} that only returns
     * a constant name.
     *
     * @param name The constant string to return
     * @return a new constant naming strategy
     */
    static NamingStrategy Static(String name) {
        return new NamingStrategy() {
            @Override
            public String generate() {
                return name;
            }

            @Override
            public Iterator<String> iterator() {
                return new Iterator<>() {
                    boolean hasNext = true;

                    @Override
                    public boolean hasNext() {
                        return hasNext;
                    }

                    @Override
                    public String next() {
                        hasNext = false;
                        return name;
                    }
                };
            }
        };
    }

    /**
     * Generate a random name postfix.
     *
     * @return a random numeric string
     */
    static String randPostfix() {
        return Long.toString(System.nanoTime());
    }
}
