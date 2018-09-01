package at.jku.ssw.java.bytecode.reducer.runtypes;

import at.jku.ssw.java.bytecode.reducer.context.Reduction.Base;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Result;
import at.jku.ssw.java.bytecode.reducer.utils.cachetypes.CodePosition;
import at.jku.ssw.java.bytecode.reducer.utils.functional.Catch;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;

import java.util.Arrays;
import java.util.Optional;

import static at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist.bytecode;

/**
 * Run type that allows low level access to method behaviour.
 * Caches code positions that were already tried.
 */
public interface InstructionReducer extends RepeatableReducer<CodePosition> {

    /**
     * Find the next applicable code positions in the given behaviour with the
     * given code iterator and perform the reduction operation on it
     *
     * @param base   The current reduction base (for accessing the cache)
     * @param method The current method
     * @param it     The code iterator that allows low level access
     * @return the potentially reduced code positions
     * @throws BadBytecode       if the byte code is invalid at some point
     * @throws NotFoundException if a type of a potential method
     *                           cannot be identified
     */
    Optional<CodePosition> reduceNext(Base<CodePosition> base,
                                      CtBehavior method,
                                      CodeIterator it) throws BadBytecode, NotFoundException;

    @Override
    default Result<CodePosition> apply(Base<CodePosition> base) throws Exception {
        final var clazz = Javassist.loadClass(base.bytecode());

        // iterate all "behaviours" (which includes methods and initializers)
        return Arrays.stream(clazz.getDeclaredBehaviors())
                .flatMap(Catch.function(method -> {
                    var m = method.getMethodInfo();

                    final var ca = m.getCodeAttribute();
                    final var it = ca.iterator();

                    /*
                        Every constructor code begins with
                        a call to the initialization method:
                        aload_0
                        invokespecial #1

                        In case of a constructor method, those first
                        two instructions are therefore skipped,
                        as the stack is again empty after this sequence.
                    */
                    it.skipConstructor();

                    // perform the operation and "stream" the result
                    // (to abide to flatMap rules)
                    var result = reduceNext(base, method, it).stream();

                    try {
                        // rebuild the stack map
                        m.rebuildStackMap(ClassPool.getDefault());
                    } catch (BadBytecode e) {
                        // if rebuild fails, this means that the bytecode is
                        // invalid and will fail the test anyway
                        // TODO find better way to skip this
                    }

                    return result;
                }))
                .findAny()
                .map(Catch.function(cp -> base.toResult(bytecode(clazz), cp)))
                .orElseGet(base::toMinimalResult);
    }

}
