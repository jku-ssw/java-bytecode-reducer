package at.jku.ssw.java.bytecode.reducer.modules.legacy;

import at.jku.ssw.java.bytecode.reducer.modules.methods.RemoveUnusedMethods;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RemoveUnusedMethodsTest extends ReducerTest<RemoveUnusedMethods> {

    @BeforeEach
    void setUp() {
        reducer = new RemoveUnusedMethods();
    }

    @AfterEach
    void tearDown() {
        reducer = null;
    }

    @Test
    void testNoUsedMethods() throws Exception {
        assertReduced("NoUsedMethods");
    }

    @Test
    void testRecursiveCalls() throws Exception {
        assertReduced("RecursiveCalls");
    }

}
