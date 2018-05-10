package at.jku.ssw.java.bytecode.reducer.modules;

import at.jku.ssw.java.bytecode.reducer.context.Reduction;
import at.jku.ssw.java.bytecode.reducer.runtypes.RepeatableReducer;

public class FieldsToLocals implements RepeatableReducer<Object> {
    // TODO

    @Override
    public Reduction.Result<Object> apply(Reduction.Base<Object> base) throws Exception {
        return null;
    }

    @Override
    public Reduction.Result<Object> force(byte[] bytecode) throws Exception {
        return null;
    }

    @Override
    public byte[] apply(byte[] bytecode) throws Exception {
        return new byte[0];
    }
}
