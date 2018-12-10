package at.jku.ssw.java.bytecode.reducer.modules.methods;

import at.jku.ssw.java.bytecode.reducer.annot.Expensive;
import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.states.State;
import at.jku.ssw.java.bytecode.reducer.runtypes.ForcibleReducer;
import at.jku.ssw.java.bytecode.reducer.utils.functional.Catch;
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
 * Remove void method calls.
 */
@Expensive
@Unsound
public class RemoveVoidMethodCalls implements ForcibleReducer<Integer> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public State.Experimental<Integer> apply(State.Stable<Integer> stable) throws Exception {
        final CtClass clazz = Javassist.loadClass(stable.bytecode());

        final AtomicReference<MethodCall> call = new AtomicReference<>();

        Instrumentation.forMethodCalls(
                clazz,
                Catch.predicate(c ->
                        c.getMethod().getReturnType().equals(CtClass.voidType) &&
                                stable.isNotCached(c.indexOfBytecode()) &&
                                call.compareAndSet(null, c)),
                Catch.consumer(c -> {

                    logger.debug(
                            "Removing call of method '{}' at index {}",
                            c.getMethodName(),
                            c.indexOfBytecode()
                    );

                    c.replace(Expressions.NO_EXPRESSION);
                })
        );

        // if no applicable member was found, the reduction is minimal
        return Optional.ofNullable(call.get())
                .map(Catch.function(f ->
                        stable.toResult(Javassist.bytecode(clazz), f.indexOfBytecode())))
                .orElseGet(stable::toMinimalResult);
    }
}
