package at.jku.ssw.java.bytecode.reducer.modules.remove.local.self.assignments;

import at.jku.ssw.java.bytecode.reducer.runtypes.ASMReducer;
import org.objectweb.asm.ClassVisitor;

import java.util.stream.Stream;

public class RemoveLocalSelfAssignments implements ASMReducer {

    @Override
    public Stream<ClassVisitor> visitors(ClassVisitor parent) {
        return Stream.of(new ClassAdapter(parent));
    }

}
