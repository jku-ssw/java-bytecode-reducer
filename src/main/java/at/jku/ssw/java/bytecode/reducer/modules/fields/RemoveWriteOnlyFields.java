package at.jku.ssw.java.bytecode.reducer.modules.fields;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.runtypes.JavassistHelper;
import at.jku.ssw.java.bytecode.reducer.runtypes.MemberReducer;
import at.jku.ssw.java.bytecode.reducer.utils.functional.Catch;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Expressions;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Instrumentation;
import javassist.CtClass;
import javassist.CtField;
import javassist.expr.FieldAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Stream;

@Sound
public class RemoveWriteOnlyFields
        implements MemberReducer<CtClass, CtField, String>, JavassistHelper {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Stream<CtField> getMembers(CtClass clazz) throws Exception {
        return Instrumentation.unusedFields(clazz, FieldAccess::isWriter);
    }

    @Override
    public CtClass process(CtClass clazz, CtField field) throws Exception {
        Instrumentation.forFieldAccesses(clazz,
                Catch.predicate(fa -> fa.getField().getName().equals(field.getName())),
                Catch.consumer(fa -> {
                    logger.debug(
                            "Replacing field access '{}' at index {}",
                            fa.getFieldName(),
                            fa.indexOfBytecode()
                    );

                    // remove write access locations
                    fa.replace(Expressions.NO_EXPRESSION);
                })
        );

        logger.debug("Removing field '{}'", field.getName());
        clazz.removeField(field);

        return clazz;
    }

    @Override
    public String keyFromMember(CtField ctField) {
        return ctField.getName();
    }
}
