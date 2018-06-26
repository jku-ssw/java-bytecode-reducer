package at.jku.ssw.java.bytecode.reducer.modules.fields;

import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.runtypes.JavassistHelper;
import at.jku.ssw.java.bytecode.reducer.runtypes.MemberReducer;
import at.jku.ssw.java.bytecode.reducer.utils.MemberAttribute;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Members;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Attempts to remove randomly selected attributes from fields.
 */
@Unsound
public class RemoveFieldAttributes
        implements MemberReducer<CtClass, MemberAttribute>, JavassistHelper {

    private static final Logger logger = LogManager.getLogger();

    /**
     * Maximum number of modifiers that are attempted for each field.
     */
    private static final int MAX_ATTEMPTS = 5;

    @Override
    public Stream<MemberAttribute> getMembers(CtClass clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .flatMap(f ->
                        Members.Attribute.randStream()
                                .limit(MAX_ATTEMPTS)
                                .map(a -> a.flag)
                                .filter(a -> (f.getModifiers() & a) != 0)
                                .map(a -> new MemberAttribute(f.getName(), a))
                );
    }

    @Override
    public CtClass process(CtClass clazz, MemberAttribute memAttr)
            throws NotFoundException {

        logger.debug(
                "Removing attribute '{}' of field '{}'",
                Modifier.toString(memAttr.attribute),
                memAttr.member
        );

        var field = clazz.getDeclaredField(memAttr.member);

        field.setModifiers(Modifier.clear(field.getModifiers(), memAttr.attribute));

        return clazz;
    }
}
