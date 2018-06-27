package at.jku.ssw.java.bytecode.reducer.modules.fields;

import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.context.Reduction;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Base;
import at.jku.ssw.java.bytecode.reducer.runtypes.AssignmentReplacer;
import at.jku.ssw.java.bytecode.reducer.runtypes.RepeatableReducer;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TConsumer;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TFunction;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TPredicate;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Expressions;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Instrumentation;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.expr.FieldAccess;

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
public class RemoveReadOnlyFields implements RepeatableReducer<CtField>, AssignmentReplacer {

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
                (TPredicate<FieldAccess>) fa -> fa.getField().equals(field),
                (TConsumer<FieldAccess>) fa -> {
                    // read access is replaced by default values while
                    // write access (initial assignment in constructor) is removed
                    if (fa.isReader())
                        fa.replace(replaceWith(value));
                    else
                        fa.replace("");
                }
        );

        return base.toResult(Javassist.bytecode(clazz), field);
    }

    @Override
    public Reduction.Result<CtField> force(byte[] bytecode) throws Exception {
        CtClass clazz = Javassist.loadClass(bytecode);

        Map<CtField, String> defaultValues = eligibleFields(clazz)
                .collect(Collectors.toMap(
                        Function.identity(),
                        (TFunction<CtField, String>) f ->
                                Expressions.defaults(f.getType())));

        Instrumentation.forFieldAccesses(clazz,
                (TPredicate<FieldAccess>) fa ->
                        defaultValues.containsKey(fa.getField()),
                (TConsumer<FieldAccess>) fa -> {
                    if (fa.isReader())
                        fa.replace(replaceWith(defaultValues.get(fa.getField())));
                    else
                        fa.replace("");
                });

        defaultValues.keySet()
                .forEach((TConsumer<CtField>) clazz::removeField);

        Base<CtField> base = Reduction.of(Javassist.bytecode(clazz));

        return base.toMinimalResult();
    }

}
