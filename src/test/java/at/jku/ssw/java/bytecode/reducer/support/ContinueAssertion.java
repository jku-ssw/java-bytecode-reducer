package at.jku.ssw.java.bytecode.reducer.support;

import java.util.function.Consumer;

public class ContinueAssertion {
    private final byte[] bytecode;

    public static ContinueAssertion with(byte[] bytecode) {
        return new ContinueAssertion(bytecode);
    }

    private ContinueAssertion(byte[] bytecode) {
        this.bytecode = bytecode;
    }

    public ContinueAssertion and(Consumer<byte[]> assertion) {
        assertion.accept(bytecode);

        return this;
    }
}
