package at.jku.ssw.java.bytecode.reducer.modules;

import at.jku.ssw.java.bytecode.reducer.modules.remove.local.self.assignments.RemoveLocalSelfAssignments;
import at.jku.ssw.java.bytecode.reducer.support.VersionedTest;
import org.objectweb.asm.Label;
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
        mv.visitInsn(RETURN);

        mv.visitMaxs(1, 5);
        mv.visitEnd();

        assertThat(
                RemoveLocalSelfAssignments.class,
                reduces(SimpleLocalVariableOperations.multipleAssignments(version))
                        .to(cw.toByteArray())
        );
    }

    @VersionedTest
    public void testLoopSelfAssignment(int version) {
        var cw = SimpleLocalVariableOperations.clazz(version);

        var mv = cw.visitMethod(ACC_PUBLIC, "assignmentsWithLoop", "(I)I", null, null);
        Label loopBegin = new Label();
        Label loopEnd = new Label();

        mv.visitCode();
        mv.visitInsn(ICONST_5);
        mv.visitVarInsn(ISTORE, 2);
        mv.visitLabel(loopBegin);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitJumpInsn(IFLE, loopEnd);
        mv.visitInsn(ICONST_1);
        mv.visitVarInsn(ISTORE, 3);
        mv.visitIincInsn(2, -1);
        mv.visitJumpInsn(GOTO, loopBegin);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitInsn(IRETURN);

        mv.visitMaxs(1, 4);
        mv.visitEnd();

        assertThat(
                RemoveLocalSelfAssignments.class,
                reduces(SimpleLocalVariableOperations.assignmentsWithLoop(version))
                        .to(cw.toByteArray())
        );
    }

}
