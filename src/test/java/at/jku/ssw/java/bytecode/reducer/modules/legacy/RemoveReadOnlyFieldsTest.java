package at.jku.ssw.java.bytecode.reducer.modules.legacy;

import at.jku.ssw.java.bytecode.reducer.modules.fields.RemoveReadOnlyFields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RemoveReadOnlyFieldsTest extends ReducerTest<RemoveReadOnlyFields> {

    @BeforeEach
    void setUp() {
        reducer = new RemoveReadOnlyFields();
    }

    @AfterEach
    void tearDown() {
        reducer = null;
    }

    @Test
    void testNoFields() throws Exception {
        assertReduced("NoFields");
    }

    @Test
    void testWrapperTypeFields() throws Exception {
        assertReduced("WrapperTypeFields");
    }

    @Test
    void testUnusedFields() throws Exception {
        assertReduced("UnusedFields");
    }

    @Test
    void testWriteOnlyFields() throws Exception {
        assertReduced("WriteOnlyFields");
    }
}
