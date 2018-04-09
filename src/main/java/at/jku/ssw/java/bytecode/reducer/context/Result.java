package at.jku.ssw.java.bytecode.reducer.context;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * Represents a generic reduction result.
 * Contains the bytes of the reduced file as well as information about
 * generic attempts.
 *
 * @param <U> The type of attempts that are reported
 */
public final class Result<U> {
    /**
     * The resulting bytes of the reduction operation.
     */
    private final byte[] bytes;

    /**
     * The attempted steps.
     */
    private final Set<U> attempted;

    /**
     * Initializes a result for the given bytes and the given attempts.
     *
     * @param bytes     The reduction result
     * @param attempted The attempted steps
     */
    public Result(byte[] bytes, Set<U> attempted) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
        this.attempted = Collections.unmodifiableSet(attempted);
    }

    /**
     * Initializes a result with no attempts.
     *
     * @param bytes
     */
    private Result(byte[] bytes) {
        this(bytes, Set.of());
    }

    public Result<U> ok() {
        return new Result<>(bytes);
    }

    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    public Set<U> getAttempted() {
        return attempted;
    }
}
