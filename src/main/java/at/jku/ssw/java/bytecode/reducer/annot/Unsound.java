package at.jku.ssw.java.bytecode.reducer.annot;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Descriptive annotation to signal that a transformation is not sound.
 * This indicates potentially code / result breaking changes.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Unsound {
}
