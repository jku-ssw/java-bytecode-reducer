package at.jku.ssw.java.bytecode.reducer.modules;

import at.jku.ssw.java.bytecode.reducer.modules.remove.field.self.assignments.RemoveFieldSelfAssignments;
import at.jku.ssw.java.bytecode.reducer.support.VersionedTest;
import samples.SimpleFieldOperations;

import static at.jku.ssw.java.bytecode.reducer.modules.ReducerTest.ReducesTo.reduces;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

public class RemoveFieldSelfAssignmentsTest extends ReducerTest {

    @VersionedTest
    public void testSimpleSelfAssignment(int version) {

        var instance = new SimpleFieldOperations();
        var cw = instance.assemble(version);

        var mv1 = cw.visitMethod(ACC_PUBLIC, "assignSelfToField", "()V", null, null);
        mv1.visitCode();
        mv1.visitMaxs(0, 1);
        mv1.visitEnd();

        instance.assign10ToField(cw);

        var mv2 = cw.visitMethod(ACC_PUBLIC, "assignSelfToFieldDup", "()V", null, null);
        mv2.visitCode();
        mv2.visitMaxs(0, 1);
        mv2.visitEnd();

        assertThat(
                RemoveFieldSelfAssignments.class,
                reduces(SimpleFieldOperations.bytecode(version))
                        .to(cw.toByteArray())
        );
    }

}
