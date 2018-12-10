package at.jku.ssw.java.bytecode.reducer.modules.legacy;

import at.jku.ssw.java.bytecode.reducer.modules.methods.RemoveMethodAttributes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RemoveMethodAttributesTest extends ReducerTest<RemoveMethodAttributes> {

    @BeforeEach
    void setUp() {
        reducer = new RemoveMethodAttributes();
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
