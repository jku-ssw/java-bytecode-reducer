package at.jku.ssw.java.bytecode.reducer.modules.fields;

import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.context.Reduction;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Base;
import at.jku.ssw.java.bytecode.reducer.runtypes.RepeatableReducer;
import at.jku.ssw.java.bytecode.reducer.utils.Javassist;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TConsumer;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TFunction;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TPredicate;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.expr.FieldAccess;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.reducer.utils.Javassist.isInitializer;
import static at.jku.ssw.java.bytecode.reducer.utils.Javassist.isMemberOfClass;

/**
 * Removes read-only fields and replaces their accessors with the default value
 * for each type.
 */
@Unsound
public class RemoveReadOnlyFields implements RepeatableReducer<CtField> {

    private static final String PATTERN = "$_ = ";

    private static String replaceWith(String value) {
        return PATTERN + value;
    }

    private Stream<CtField> eligibleFields(CtClass clazz) throws CannotCompileException {
        return Javassist.unusedFields(clazz, f ->
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
        String  value = Javassist.defaults(field.getType());

        Javassist.forFieldAccesses(clazz,
                fa -> !fa.isWriter(),
                (TConsumer<FieldAccess>) fa -> fa.replace(replaceWith(value))
        );

        return base.toResult(Javassist.bytecode(clazz), field);
    }

    @Override
    public Reduction.Result<CtField> force(byte[] bytecode) throws Exception {
        CtClass clazz = Javassist.loadClass(bytecode);

        @SuppressWarnings("unchecked")
        Map<CtField, String> defaultValues = eligibleFields(clazz)
                .collect(Collectors.toMap(
                        Function.identity(),
                        (TFunction<CtField, String>) f ->
                                Javassist.defaults(f.getType())));

        Javassist.forFieldAccesses(clazz,
                (TPredicate<FieldAccess>) fa ->
                        !fa.isWriter() && defaultValues.containsKey(fa.getField()),
                (TConsumer<FieldAccess>) fa ->
                        fa.replace(replaceWith(defaultValues.get(fa.getField()))));

        defaultValues.keySet().forEach(f -> {
            try {
                clazz.removeField(f);
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        });

        Base<CtField> base = Reduction.of(Javassist.bytecode(clazz));

        return base.toMinimalResult();
    }

}
