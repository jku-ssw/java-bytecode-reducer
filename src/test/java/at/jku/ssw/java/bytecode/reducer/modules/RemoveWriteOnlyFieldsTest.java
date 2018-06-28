package at.jku.ssw.java.bytecode.reducer.modules;

import at.jku.ssw.java.bytecode.reducer.modules.fields.RemoveWriteOnlyFields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RemoveWriteOnlyFieldsTest extends ReducerTest<RemoveWriteOnlyFields> {

    @BeforeEach
    void setUp() {
        reducer = new RemoveWriteOnlyFields();
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
