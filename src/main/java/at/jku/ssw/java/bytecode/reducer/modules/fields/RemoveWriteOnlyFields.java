package at.jku.ssw.java.bytecode.reducer.modules.fields;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.runtypes.FieldReducer;
import javassist.CtClass;
import javassist.CtField;
import javassist.expr.FieldAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.reducer.utils.Javassist.*;

@Sound
public class RemoveWriteOnlyFields implements FieldReducer<CtClass, CtField> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public CtClass classFrom(byte[] bytecode) throws Exception {
        return loadClass(bytecode);
    }

    @Override
    public byte[] bytecodeFrom(CtClass clazz) throws Exception {
        return bytecode(clazz);
    }

    @Override
    public Stream<CtField> eligibleFields(CtClass clazz) throws Exception {
        return unusedFields(clazz, FieldAccess::isWriter);
    }

    @Override
    public CtClass handleField(CtClass clazz, CtField field) throws Exception {
        logger.debug("Removing field '{}'", field.getSignature());
        clazz.removeField(field);
        return clazz;
    }
}
