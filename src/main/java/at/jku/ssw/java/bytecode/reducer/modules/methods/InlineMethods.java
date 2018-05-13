package at.jku.ssw.java.bytecode.reducer.modules.methods;

import at.jku.ssw.java.bytecode.reducer.context.Reduction;
import at.jku.ssw.java.bytecode.reducer.runtypes.RepeatableReducer;
import javassist.CtMethod;

// TODO
public class InlineMethods implements RepeatableReducer<CtMethod> {

    @Override
    public Reduction.Result<CtMethod> apply(Reduction.Base<CtMethod> base) throws Exception {
        return null;
    }

    @Override
    public Reduction.Result<CtMethod> force(byte[] bytecode) throws Exception {
        return null;
    }
}
