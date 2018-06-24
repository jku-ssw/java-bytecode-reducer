package at.jku.ssw.java.bytecode.reducer.modules.fields;

import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.runtypes.JavassistHelper;
import at.jku.ssw.java.bytecode.reducer.runtypes.MemberReducer;
import javassist.CtClass;
import javassist.CtField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.stream.Stream;

@Unsound
public class RemoveFieldAttributes
        implements MemberReducer<CtClass, CtField>, JavassistHelper {

    public static final int NO_ATTRIBUTES = 0x0;

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Stream<CtField> getMembers(CtClass clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.getModifiers() != NO_ATTRIBUTES);
    }

    @Override
    public CtClass process(CtClass clazz, CtField field) {
        logger.debug("Removing attributes of field '{}'", field.getName());

        field.setModifiers(NO_ATTRIBUTES);

        return clazz;
    }
}
