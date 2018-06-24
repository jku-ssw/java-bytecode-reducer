package at.jku.ssw.java.bytecode.reducer.modules.methods;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.runtypes.JavassistHelper;
import at.jku.ssw.java.bytecode.reducer.runtypes.MemberReducer;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Expressions;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Instrumentation;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Members;
import javassist.CtClass;
import javassist.CtMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Stream;

@Sound
public class RemoveUnusedMethods
        implements MemberReducer<CtClass, CtMethod>, JavassistHelper {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Stream<CtMethod> getMembers(CtClass clazz) throws Exception {
        return Instrumentation.unusedMethods(clazz, Expressions::isRecursion)
                .filter(m -> !Members.isMain(m));
    }

    @Override
    public CtClass process(CtClass clazz, CtMethod m) throws Exception {
        logger.debug("Removing method '{}'", m.getLongName());
        clazz.removeMethod(m);
        return clazz;
    }
}
