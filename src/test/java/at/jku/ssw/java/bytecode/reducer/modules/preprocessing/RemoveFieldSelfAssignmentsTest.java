package at.jku.ssw.java.bytecode.reducer.modules.preprocessing;

import at.jku.ssw.java.bytecode.reducer.modules.preprocessing.remove.field.self.assignments.RemoveFieldSelfAssignments;
import at.jku.ssw.java.bytecode.reducer.support.VersionedTest;
import samples.SimpleFieldOperations;

import static at.jku.ssw.java.bytecode.reducer.modules.preprocessing.ReducerTest.ReducesTo.reduces;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

public class RemoveFieldSelfAssignmentsTest extends ReducerTest {

    @VersionedTest
    public void testSimpleSelfAssignment(int version) {

        var cw = SimpleFieldOperations.clazz(version);
        var mv = cw.visitMethod(ACC_PUBLIC, "bar", "()V", null, null);
        mv.visitCode();
        mv.visitMaxs(0, 1);
        mv.visitEnd();

        assertThat(
                RemoveFieldSelfAssignments.class,
                reduces(SimpleFieldOperations.bytecode(version))
                        .to(cw.toByteArray())
        );
    }
}
