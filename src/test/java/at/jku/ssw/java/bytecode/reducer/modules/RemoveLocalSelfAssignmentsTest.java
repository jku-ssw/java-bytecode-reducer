package at.jku.ssw.java.bytecode.reducer.modules;

import at.jku.ssw.java.bytecode.reducer.modules.remove.local.self.assignments.RemoveLocalSelfAssignments;
import at.jku.ssw.java.bytecode.reducer.support.VersionedTest;
import samples.SimpleLocalVariableOperations;

import static at.jku.ssw.java.bytecode.reducer.modules.ReducerTest.ReducesTo.reduces;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.objectweb.asm.Opcodes.*;

public class RemoveLocalSelfAssignmentsTest extends ReducerTest {

    @VersionedTest
    public void testSimpleSelfAssignment(int version) {

        var cw = SimpleLocalVariableOperations.clazz(version);

        var mv = cw.visitMethod(ACC_PUBLIC, "multipleAssignments", "()V", null, null);
        mv.visitCode();
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitVarInsn(ISTORE, 2);
        mv.visitInsn(ICONST_2);
        mv.visitVarInsn(ISTORE, 3);
        mv.visitLdcInsn(99);
        mv.visitVarInsn(ISTORE, 4);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitVarInsn(ISTORE, 4);

        mv.visitMaxs(1, 5);
        mv.visitEnd();

        assertThat(
                RemoveLocalSelfAssignments.class,
                reduces(SimpleLocalVariableOperations.bytecode(version))
                        .to(cw.toByteArray())
        );
    }

}
