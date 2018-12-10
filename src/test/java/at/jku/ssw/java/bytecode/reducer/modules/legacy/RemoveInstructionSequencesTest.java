package at.jku.ssw.java.bytecode.reducer.modules.legacy;

import at.jku.ssw.java.bytecode.reducer.modules.flow.RemoveInstructionSequences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RemoveInstructionSequencesTest extends ReducerTest<RemoveInstructionSequences> {

    @BeforeEach
    void setUp() {
        reducer = new RemoveInstructionSequences();
    }

    @AfterEach
    void tearDown() {
        reducer = null;
    }

    @Test
    void testLocalVariableAssignments() throws Exception {
        assertReduced("LocalVariableAssignments", true);
    }

    @Test
    void testRecursiveCalls() throws Exception {
        assertReduced("RecursiveCalls", true);
    }
}
