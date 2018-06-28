package at.jku.ssw.java.bytecode.reducer.modules;

import at.jku.ssw.java.bytecode.reducer.modules.methods.RemoveEmptyMethods;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RemoveEmptyMethodsTest extends ReducerTest<RemoveEmptyMethods> {

    @BeforeEach
    void setUp() {
        reducer = new RemoveEmptyMethods();
    }

    @AfterEach
    void tearDown() {
        reducer = null;
    }

    @Test
    void testPublicMethods() throws Exception {
        assertReduced("PublicMethods");
    }

    @Test
    void testAllAttributes() throws Exception {
        assertReduced("AllAttributes");
    }
}
