package at.jku.ssw.java.bytecode.reducer.modules.legacy;

import at.jku.ssw.java.bytecode.reducer.modules.methods.RemoveVoidMethodCalls;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RemoveVoidMethodCallsTest extends ReducerTest<RemoveVoidMethodCalls> {

    @BeforeEach
    void setUp() {
        reducer = new RemoveVoidMethodCalls();
    }

    @AfterEach
    void tearDown() {
        reducer = null;
    }

    @Test
    void testSingleMethodCall() throws Exception {
        assertReduced("SingleMethodCall")
                .and(bytes -> assertNoMethodCall(bytes, "SingleMethodCall.aVoid()"))
                .and(bytes -> assertNoMethodCall(bytes, "SingleMethodCall.main(String[])"));
    }

    @Test
    void testRecursiveCalls() throws Exception {
        assertReduced("RecursiveCalls")
                .and(bytes -> assertNoMethodCall(bytes, "RecursiveCalls.aMethod()"))
                .and(bytes -> assertNoMethodCall(bytes, "RecursiveCalls.anotherMethod()"))
                .and(bytes -> assertNoMethodCall(bytes, "RecursiveCalls.aThirdMethod()"))
                .and(bytes -> assertNoMethodCall(bytes, "RecursiveCalls.main(String[])"));
    }
}
