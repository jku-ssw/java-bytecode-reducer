package at.jku.ssw.java.bytecode.reducer.modules.methods;

import at.jku.ssw.java.bytecode.reducer.annot.Expensive;
import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.states.State;
import at.jku.ssw.java.bytecode.reducer.utils.functional.Catch;
import at.jku.ssw.java.bytecode.reducer.runtypes.ForcibleReducer;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Expressions;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Instrumentation;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist;
import javassist.CtClass;
import javassist.expr.MethodCall;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Replaces method calls with default values of the corresponding return types.
 * This reducer ignores {@code void} methods.
 */
@Expensive
@Unsound
public class ReplaceMethodCalls implements ForcibleReducer<Integer> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public State.Experimental<Integer> apply(State.Stable<Integer> stable) throws Exception {
        final CtClass clazz = Javassist.loadClass(stable.bytecode());

        final AtomicReference<MethodCall> call = new AtomicReference<>();

        Instrumentation.forMethodCalls(
                clazz,
                Catch.predicate(c ->
                        !c.getMethod().getReturnType().equals(CtClass.voidType) &&
                                stable.isNotCached(c.indexOfBytecode()) &&
                                call.compareAndSet(null, c)),
                Catch.consumer(c -> {
                    CtClass type  = c.getMethod().getReturnType();
                    var     value = Expressions.defaults(type);

                    logger.debug(
                            "Replacing call of method '{}' at index {} with '{}'",
                            c.getMethodName(),
                            c.indexOfBytecode(),
                            value
                    );

                    c.replace(Expressions.replaceAssign(value));
                })
        );

        // if no applicable member was found, the reduction is minimal
        return Optional.ofNullable(call.get())
                .map(Catch.function(f ->
                        stable.toResult(Javassist.bytecode(clazz), f.indexOfBytecode())))
                .orElseGet(stable::toMinimalResult);
    }
}
