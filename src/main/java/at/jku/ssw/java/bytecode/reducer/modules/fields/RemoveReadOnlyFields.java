package at.jku.ssw.java.bytecode.reducer.modules.fields;

import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.states.Reduction;
import at.jku.ssw.java.bytecode.reducer.states.Reduction.Base;
import at.jku.ssw.java.bytecode.reducer.utils.functional.Catch;
import at.jku.ssw.java.bytecode.reducer.runtypes.ForcibleReducer;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Expressions;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Instrumentation;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.reducer.utils.javassist.Members.isInitializer;
import static at.jku.ssw.java.bytecode.reducer.utils.javassist.Members.isMemberOfClass;

/**
 * Removes read-only fields and replaces their accessors with the default value
 * for each type.
 */
@Unsound
public class RemoveReadOnlyFields implements ForcibleReducer<CtField> {

    private static final Logger logger = LogManager.getLogger();

    private Stream<CtField> eligibleFields(CtClass clazz) throws CannotCompileException {
        return Instrumentation.unusedFields(clazz, f ->
                f.isReader()
                        || isInitializer(f.where())
                        && isMemberOfClass(f.where(), clazz)
                        && f.isWriter());
    }

    @Override
    public Reduction.Result<CtField> apply(Base<CtField> base) throws Exception {
        CtClass clazz = Javassist.loadClass(base.bytecode());

        Optional<CtField> optField = eligibleFields(clazz).findFirst();

        if (!optField.isPresent())
            return base.toMinimalResult();

        CtField field = optField.get();
        String  value = Expressions.defaults(field.getType());

        Instrumentation.forFieldAccesses(clazz,
                Catch.predicate(fa -> fa.getField().equals(field)),
                Catch.consumer(fa -> {
                    logger.debug(
                            "Replacing field access '{}' at index {} with '{}'",
                            fa.getFieldName(),
                            fa.indexOfBytecode(),
                            value
                    );

                    // read access is replaced by default values while
                    // write access (initial assignment in constructor) is removed
                    if (fa.isReader())
                        fa.replace(Expressions.replaceAssign(value));
                    else
                        fa.replace(Expressions.NO_EXPRESSION);
                })
        );

        return base.toResult(Javassist.bytecode(clazz), field);
    }

    @Override
    public Reduction.Result<CtField> force(byte[] bytecode) throws Exception {
        CtClass clazz = Javassist.loadClass(bytecode);

        Map<CtField, String> defaultValues = eligibleFields(clazz)
                .collect(Collectors.toMap(
                        Function.identity(),
                        Catch.function(f ->
                                Expressions.defaults(f.getType()))));

        Instrumentation.forFieldAccesses(clazz,
                Catch.predicate(fa ->
                        defaultValues.containsKey(fa.getField())),
                Catch.consumer(fa -> {
                    var value = defaultValues.get(fa.getField());

                    logger.debug(
                            "Replacing field access '{}' at index {} with '{}'",
                            fa.getFieldName(),
                            fa.indexOfBytecode(),
                            value
                    );

                    if (fa.isReader())
                        fa.replace(Expressions.replaceAssign(value));
                    else
                        fa.replace(Expressions.NO_EXPRESSION);
                }));

        defaultValues.keySet()
                .forEach(Catch.consumer(clazz::removeField));

        Base<CtField> base = Reduction.of(Javassist.bytecode(clazz));

        return base.toMinimalResult();
    }

}
