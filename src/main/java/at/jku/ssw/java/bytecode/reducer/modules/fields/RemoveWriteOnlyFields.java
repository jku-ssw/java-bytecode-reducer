package at.jku.ssw.java.bytecode.reducer.modules.fields;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.runtypes.JavassistHelper;
import at.jku.ssw.java.bytecode.reducer.runtypes.InstanceCachedMemberReducer;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Instrumentation;
import javassist.CtClass;
import javassist.CtField;
import javassist.expr.FieldAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Stream;

@Sound
public class RemoveWriteOnlyFields
        implements InstanceCachedMemberReducer<CtClass, CtField>, JavassistHelper {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Stream<CtField> getMembers(CtClass clazz) throws Exception {
        return Instrumentation.unusedFields(clazz, FieldAccess::isWriter);
    }

    @Override
    public CtClass process(CtClass clazz, CtField field) throws Exception {
        logger.debug("Removing field '{}'", field.getName());
        clazz.removeField(field);
        return clazz;
    }
}
