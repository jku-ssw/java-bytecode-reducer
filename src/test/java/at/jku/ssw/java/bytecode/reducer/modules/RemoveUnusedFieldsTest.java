package at.jku.ssw.java.bytecode.reducer.modules;

import at.jku.ssw.java.bytecode.reducer.modules.fields.RemoveUnusedFields;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RemoveUnusedFieldsTest extends ReducerTest<RemoveUnusedFields> {

    @BeforeEach
    void setUp() {
        reducer = new RemoveUnusedFields();
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
    void testNoUnusedFields() throws Exception {
        assertReduced("NoUnusedFields");
    }

    @Test
    void testNoUsedFields() throws Exception {
        assertReduced("NoUsedFields")
                .and((TConsumer<byte[]>) this::assertNoFieldAccess);
    }

    @Test
    void testStaticFields() throws Exception {
        // TODO
    }

    @Test
    void testPrivateFields() throws Exception {
        // TODO
    }

    @Test
    void testPublicFields() {
        // TODO
    }
}
