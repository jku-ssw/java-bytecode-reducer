package at.jku.ssw.java.bytecode.reducer.modules.methods;

import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.runtypes.MemberReducer;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Members;
import javassist.CtClass;
import javassist.CtMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist.bytecode;
import static at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist.loadClass;

@Unsound
public class RemoveMethodAttributes implements MemberReducer<CtClass, CtMethod> {

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
    public Stream<CtMethod> getMembers(CtClass clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> !Members.isMain(m));
    }

    @Override
    public CtClass process(CtClass clazz, CtMethod method) {
        logger.debug("Removing attributes of method '{}'", method.getLongName());

        method.setModifiers(NO_ATTRIBUTES);

        return clazz;
    }
}
