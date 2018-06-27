package at.jku.ssw.java.bytecode.reducer.annot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to highlight expensive reducers that should probably called
 * after other, probably lighter reduction operations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Expensive {
}
