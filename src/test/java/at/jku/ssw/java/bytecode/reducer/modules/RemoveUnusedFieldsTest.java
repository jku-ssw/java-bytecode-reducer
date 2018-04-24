package at.jku.ssw.java.bytecode.reducer.modules;

import at.jku.ssw.java.bytecode.reducer.Javassist;
import javassist.CtClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RemoveUnusedFieldsTest extends ReducerTest<RemoveUnusedFields> {

    private void assertReduced(final String className) throws Exception {
        byte[] original = loadOriginalBytecode(className);

        byte[] expectedBytecode = loadReducedBytecode(className);

        byte[] reducedBytecode = reducer.getMinimal(original, __ -> true);

        CtClass expected = classFromBytecode(expectedBytecode);
        CtClass actual   = classFromBytecode(reducedBytecode);

        assertTrue(Javassist.equals(expected, actual));
    }

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
    void testOnlyUsedFields() throws Exception {
        // TODO
    }

    @Test
    void testOnlyUnusedFields() throws Exception {
        // TODO
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
