package at.jku.ssw.java.bytecode.reducer.modules.fields;

import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.runtypes.JavassistHelper;
import at.jku.ssw.java.bytecode.reducer.runtypes.MemberReducer;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.stream.Stream;

@Unsound
public class RemoveStaticFieldAttributes
        implements MemberReducer<CtClass, CtField, String>, JavassistHelper {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Stream<CtField> getMembers(CtClass clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()));
    }

    @Override
    public CtClass process(CtClass clazz, CtField field) {
        logger.debug("Removing static modifier for field '{}'", field.getName());

        field.setModifiers(Modifier.clear(field.getModifiers(), Modifier.STATIC));

        return clazz;
    }

    @Override
    public String keyFromMember(CtField ctField) {
        return ctField.getName();
    }
}
