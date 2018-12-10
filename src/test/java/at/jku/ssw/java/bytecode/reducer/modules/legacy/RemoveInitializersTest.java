package at.jku.ssw.java.bytecode.reducer.modules.legacy;

import at.jku.ssw.java.bytecode.reducer.modules.methods.RemoveInitializers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RemoveInitializersTest extends ReducerTest<RemoveInitializers> {

    @BeforeEach
    void setUp() {
        reducer = new RemoveInitializers();
    }

    @AfterEach
    void tearDown() {
        reducer = null;
    }

    @Test
    void testAllInitializers() throws Exception {
        assertReduced("AllInitializers");
    }

}
