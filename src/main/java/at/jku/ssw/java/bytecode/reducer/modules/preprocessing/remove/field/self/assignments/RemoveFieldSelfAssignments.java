package at.jku.ssw.java.bytecode.reducer.modules.preprocessing.remove.field.self.assignments;

import at.jku.ssw.java.bytecode.reducer.runtypes.ASMReducer;
import org.objectweb.asm.ClassVisitor;

import java.util.stream.Stream;

public class RemoveFieldSelfAssignments implements ASMReducer {

    @Override
    public Stream<ClassVisitor> visitors(ClassVisitor parent) {
        return Stream.of(new ClassAdapter(parent));
    }

}
