package at.jku.ssw.java.bytecode.reducer.modules.fields;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.runtypes.MemberReducer;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Instrumentation;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TConsumer;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TPredicate;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Members;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.expr.FieldAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist.*;

@Sound
public class RemoveUnusedFields implements MemberReducer<CtClass, CtField> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public CtClass classFrom(byte[] bytecode) throws IOException {
        return loadClass(bytecode);
    }

    @Override
    public byte[] bytecodeFrom(CtClass clazz) throws IOException, CannotCompileException {
        return Javassist.bytecode(clazz);
    }

    @Override
    public Stream<CtField> getMembers(CtClass clazz) throws CannotCompileException {
        return Instrumentation.unusedFields(clazz, f ->
                Members.isInitializer(f.where()) && Members.isMemberOfClass(f.where(), clazz) && f.isWriter());
    }

    @Override
    public CtClass process(CtClass clazz, CtField field) throws NotFoundException, CannotCompileException {
        logger.debug("Removing field '{}'", field.getName());

        // replaces field access in constructors with local variables
        Instrumentation.forFieldAccesses(
                clazz,
                (TPredicate<FieldAccess>) fa -> field.equals(fa.getField()),
                (TConsumer<FieldAccess>) f -> f.replace("")
        );

        clazz.removeField(field);

        return clazz;
    }

}
