package at.jku.ssw.java.bytecode.reducer.modules.fields;

import at.jku.ssw.java.bytecode.reducer.context.Reduction;
import at.jku.ssw.java.bytecode.reducer.runtypes.RepeatableReducer;
import javassist.CtField;

// TODO
public class ReplaceReadOnlyFields implements RepeatableReducer<CtField> {

    @Override
    public Reduction.Result<CtField> apply(Reduction.Base<CtField> base) throws Exception {
        return null;
    }

    @Override
    public Reduction.Result<CtField> force(byte[] bytecode) throws Exception {
        return null;
    }

    @Override
    public byte[] apply(byte[] bytecode) throws Exception {
        return new byte[0];
    }
}
