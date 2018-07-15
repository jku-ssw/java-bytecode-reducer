package at.jku.ssw.java.bytecode.reducer.runtypes;

import at.jku.ssw.java.bytecode.reducer.context.Reduction.Base;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Result;
import at.jku.ssw.java.bytecode.reducer.utils.functional.Catch;
import at.jku.ssw.java.bytecode.reducer.utils.CodePosition;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist.bytecode;

/**
 * Run type that allows low level access to method behaviour.
 * Caches code positions that were already tried.
 */
public interface InstructionReducer extends RepeatableReducer<CodePosition> {

    /**
     * Perform the reduction operation on the given code position
     * using the given iterator.
     *
     * @param clazz        The currently reduced class
     * @param behav        The current method
     * @param codePosition The code position that should be reduced
     * @param iterator     The code iterator that allows low level access
     * @return the reduced class
     */
    CtClass reduce(CtClass clazz,
                   CtBehavior behav,
                   CodePosition codePosition,
                   CodeIterator iterator);

    /**
     * Find applicable code positions in the given behaviour with the
     * given code iterator.
     *
     * @param method The current method
     * @param it     The code iterator that allows low level access
     * @return all applicable code positions
     * @throws BadBytecode if the byte code is invalid at some point
     */
    Stream<CodePosition> codePositions(CtBehavior method,
                                       CodeIterator it) throws BadBytecode;

    /**
     * Reduces the given class at the given code position by the
     * implementation dependent operation.
     *
     * @param base         The reduction base
     * @param clazz        The class to reduce
     * @param codePosition The determined code position
     * @return the reduction result
     */
    default Result<CodePosition> process(Base<CodePosition> base,
                                         CtClass clazz,
                                         CodePosition codePosition) {

        /*
        Fetch the method that is referenced in the code position.
        As this always returns a single method, the Optional result is simply
        forced.
        */
        return Arrays.stream(clazz.getDeclaredBehaviors())
                .filter(b -> b.getLongName().equals(codePosition.member))
                .findAny()
                .map(behav -> {

                    var methodInfo = behav.getMethodInfo();

                    var ca = methodInfo.getCodeAttribute();
                    var it = ca.iterator();

                    try {
                        return base.toResult(bytecode(
                                reduce(clazz, behav, codePosition, it)
                        ), codePosition);
                    } catch (IOException e) {
                        return null;
                    }
                }).orElse(null);
    }

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

                    return codePositions(method, it)
                            .filter(cp -> !base.cache().contains(cp));
                }))
                .findAny()
                .map(cp -> process(base, clazz, cp))
                .orElseGet(base::toMinimalResult);
    }

}
