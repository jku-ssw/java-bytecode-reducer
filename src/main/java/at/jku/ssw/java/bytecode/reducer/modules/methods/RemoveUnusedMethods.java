package at.jku.ssw.java.bytecode.reducer.modules.methods;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.runtypes.JavassistHelper;
import at.jku.ssw.java.bytecode.reducer.runtypes.InstanceCachedMemberReducer;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Instrumentation;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Members;
import javassist.CtClass;
import javassist.CtMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Stream;

@Sound
public class RemoveUnusedMethods
        implements InstanceCachedMemberReducer<CtClass, CtMethod>, JavassistHelper {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Stream<CtMethod> getMembers(CtClass clazz) throws Exception {
        return Instrumentation.unusedMethods(clazz, Members::isRecursion)
                .filter(Members::isNotMain);
    }

    @Override
    public CtClass process(CtClass clazz, CtMethod method) throws Exception {
        logger.debug("Removing method '{}'", method.getLongName());
        clazz.removeMethod(method);
        return clazz;
    }
}
