package at.jku.ssw.java.bytecode.reducer.modules.fields;

import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.runtypes.MemberReducer;
import javassist.CtClass;
import javassist.CtField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.reducer.utils.Javassist.bytecode;
import static at.jku.ssw.java.bytecode.reducer.utils.Javassist.loadClass;

@Unsound
public class RemoveAttributes implements MemberReducer<CtClass, CtField> {

    public static final int NO_ATTRIBUTES = 0x0;

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
    public Stream<CtField> getMembers(CtClass clazz) {
        return Arrays.stream(clazz.getDeclaredFields());
    }

    @Override
    public CtClass process(CtClass clazz, CtField field) {
        logger.debug("Removing attributes of field '{}'", field.getSignature());

        field.setModifiers(NO_ATTRIBUTES);

        return clazz;
    }
}