package at.jku.ssw.java.bytecode.reducer.modules.fields;

import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.runtypes.JavassistHelper;
import at.jku.ssw.java.bytecode.reducer.runtypes.MemberReducer;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Members;
import javassist.CtClass;
import javassist.CtField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Attempts to remove all attributes / modifiers from a class' fields.
 */
@Unsound
public class RemoveAllFieldAttributes
        implements MemberReducer<CtClass, CtField>, JavassistHelper {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Stream<CtField> getMembers(CtClass clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.getModifiers() != Members.Attribute.NONE);
    }

    @Override
    public CtClass process(CtClass clazz, CtField field) {
        logger.debug("Removing all attributes of field '{}'", field.getName());

        field.setModifiers(Members.Attribute.NONE);

        return clazz;
    }
}
