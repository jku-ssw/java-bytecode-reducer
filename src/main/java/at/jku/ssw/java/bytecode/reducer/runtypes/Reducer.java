package at.jku.ssw.java.bytecode.reducer.runtypes;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;

import java.util.Comparator;

/**
 * Represents a reduction of a class file.
 */
public interface Reducer {
    /**
     * Reduces the class file that is contained in the given bytes.
     *
     * @param bytecode The byte array that contains the byte code
     * @return the reduced byte code
     * @throws Exception if the byte code cannot be parsed or is invalid
     */
    byte[] apply(byte[] bytecode) throws Exception;

    /**
     * Static ordering of {@link Reducer} implementations.
     */
    Comparator<Class<? extends Reducer>> ORDERING = (a, b) -> {
        boolean aSound = a.isAnnotationPresent(Sound.class);
        boolean bSound = b.isAnnotationPresent(Sound.class);

        if (aSound && bSound)
            return 0;

        return aSound ? -1 : bSound ? 1 : 0;
    };
}
