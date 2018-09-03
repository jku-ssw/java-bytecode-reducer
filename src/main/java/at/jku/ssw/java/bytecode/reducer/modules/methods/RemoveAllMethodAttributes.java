package at.jku.ssw.java.bytecode.reducer.modules.methods;

import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.runtypes.InstanceCachedMemberReducer;
import at.jku.ssw.java.bytecode.reducer.runtypes.JavassistHelper;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Members;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.BadBytecode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.stream.Stream;

@Unsound
public class RemoveAllMethodAttributes
        implements InstanceCachedMemberReducer<CtClass, CtMethod>, JavassistHelper {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Stream<CtMethod> getMembers(CtClass clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(Members::isNotMain)
                .filter(m -> m.getModifiers() != Members.Attribute.NONE);
    }

    @Override
    public CtClass process(CtClass clazz, CtMethod method) throws BadBytecode {
        logger.debug("Removing all attributes of method '{}'", method.getLongName());

        Members.makeInstanceMethod(method);

        method.setModifiers(Members.Attribute.NONE);

        return clazz;
    }
}
