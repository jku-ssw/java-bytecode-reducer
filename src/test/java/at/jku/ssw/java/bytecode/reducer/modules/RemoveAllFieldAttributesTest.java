package at.jku.ssw.java.bytecode.reducer.modules;

import at.jku.ssw.java.bytecode.reducer.modules.fields.RemoveAllFieldAttributes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RemoveAllFieldAttributesTest extends ReducerTest<RemoveAllFieldAttributes> {

    @BeforeEach
    void setUp() {
        reducer = new RemoveAllFieldAttributes();
    }

    @AfterEach
    void tearDown() {
        reducer = null;
    }

    @Test
    void testPublicFields() throws Exception {
        assertReduced("PublicFields");
    }

    @Test
    void testAllAttributes() throws Exception {
        assertReduced("AllAttributes");
    }
}
