package at.jku.ssw.java.bytecode.reducer.modules.methods;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Base;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Result;
import at.jku.ssw.java.bytecode.reducer.runtypes.RepeatableReducer;
import at.jku.ssw.java.bytecode.reducer.runtypes.AssignmentReplacer;
import at.jku.ssw.java.bytecode.reducer.utils.Javassist;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TConsumer;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TFunction;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TPredicate;
import javassist.CtClass;
import javassist.expr.MethodCall;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Sound
public class ReplaceMethodCalls implements RepeatableReducer<MethodCall>, AssignmentReplacer {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Result<MethodCall> apply(Base<MethodCall> base) throws Exception {
        final CtClass clazz = Javassist.loadClass(base.bytecode());

        final AtomicReference<MethodCall> call = new AtomicReference<>();

        Javassist.forMethodCalls(
                clazz,
                (TPredicate<MethodCall>) c ->
                        !c.getMethod().getReturnType().equals(CtClass.voidType) &&
                                !base.cache().contains(c) &&
                                call.compareAndSet(null, c),
                (TConsumer<MethodCall>) c -> {
                    CtClass type = c.getMethod().getReturnType();

                    c.replace(replaceWith(Javassist.defaults(type)));
                }
        );

        // if no applicable member was found, the reduction is minimal
        return Optional.ofNullable(call.get())
                .map((TFunction<MethodCall, Result<MethodCall>>) f ->
                        base.toResult(Javassist.bytecode(clazz), f))
                .orElse(base.toMinimalResult());
    }
}
