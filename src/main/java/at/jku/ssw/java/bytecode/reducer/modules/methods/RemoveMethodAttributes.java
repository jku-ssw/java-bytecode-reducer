package at.jku.ssw.java.bytecode.reducer.modules.methods;

import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.runtypes.JavassistHelper;
import at.jku.ssw.java.bytecode.reducer.runtypes.InstanceCachedMemberReducer;
import at.jku.ssw.java.bytecode.reducer.utils.MemberAttribute;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Members;
import javassist.CtClass;
import javassist.Modifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Attempts to remove randomly selected attributes from methods.
 */
@Unsound
public class RemoveMethodAttributes
        implements InstanceCachedMemberReducer<CtClass, MemberAttribute>, JavassistHelper {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Stream<MemberAttribute> getMembers(CtClass clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(Members::isNotMain)
                .flatMap(m ->
                        Members.Attribute.randStream()
                                .map(a -> a.flag)
                                .filter(a -> (m.getModifiers() & a) != 0)
                                .map(a -> new MemberAttribute(m.getLongName(), a))
                );
    }

    @Override
    public CtClass process(CtClass clazz, MemberAttribute memAttr) {
        logger.debug(
                "Removing attribute '{}' of method '{}'",
                Modifier.toString(memAttr.attribute),
                memAttr.member
        );

        Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.getLongName().equals(memAttr.member))
                .findAny()
                .ifPresent(m ->
                        m.setModifiers(Modifier.clear(m.getModifiers(), memAttr.attribute))
                );

        return clazz;
    }
}
