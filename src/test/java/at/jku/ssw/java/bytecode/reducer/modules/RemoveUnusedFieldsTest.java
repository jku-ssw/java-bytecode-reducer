package at.jku.ssw.java.bytecode.reducer.modules;

import at.jku.ssw.java.bytecode.reducer.support.Javassist;
import javassist.CtClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RemoveUnusedFieldsTest extends ReducerTest<RemoveUnusedFields>
        implements Javassist {

    private void assertReduced(final String className) throws Exception {
        byte[] original = loadOriginalBytecode(className);

        byte[] expectedBytecode = loadReducedBytecode(className);

        byte[] reducedBytecode = reducer.getMinimal(original, __ -> true);

        CtClass expected = classFromBytecode(expectedBytecode);
        CtClass actual   = classFromBytecode(reducedBytecode);

        assertClassEquals(expected, actual);
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
    void testNoUnusedFields() throws Exception {
        assertReduced("NoUnusedFields");
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
