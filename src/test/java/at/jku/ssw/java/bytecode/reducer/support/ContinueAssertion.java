package at.jku.ssw.java.bytecode.reducer.support;

import at.jku.ssw.java.bytecode.reducer.utils.functional.TConsumer;

public class ContinueAssertion {
    private final byte[] bytecode;

    public static ContinueAssertion with(byte[] bytecode) {
        return new ContinueAssertion(bytecode);
    }

    private ContinueAssertion(byte[] bytecode) {
        this.bytecode = bytecode;
    }

    public ContinueAssertion and(TConsumer<byte[]> assertion) throws Exception {
        assertion.accept(bytecode);

        return this;
    }
}
