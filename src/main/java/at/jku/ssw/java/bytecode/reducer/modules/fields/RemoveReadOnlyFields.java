package at.jku.ssw.java.bytecode.reducer.modules.fields;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.context.Reduction;
import at.jku.ssw.java.bytecode.reducer.runtypes.RepeatableReducer;
import at.jku.ssw.java.bytecode.reducer.utils.Javassist;
import at.jku.ssw.java.bytecode.reducer.utils.TConsumer;
import at.jku.ssw.java.bytecode.reducer.utils.TFunction;
import at.jku.ssw.java.bytecode.reducer.utils.Types;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static at.jku.ssw.java.bytecode.reducer.utils.Javassist.isInitializer;
import static at.jku.ssw.java.bytecode.reducer.utils.Javassist.isMemberOfClass;

/**
 * Removes read-only fields and replaces their accessors with the default value
 * for each type.
 */
@Sound
public class RemoveReadOnlyFields implements RepeatableReducer<CtField> {

    @Override
    public Reduction.Result<CtField> apply(Reduction.Base<CtField> base) throws Exception {

        return null;
    }

    @Override
    public Reduction.Result<CtField> force(byte[] bytecode) throws Exception {
        CtClass clazz = Javassist.loadClass(bytecode);

        @SuppressWarnings("unchecked")
        Map<CtField, Object> fieldsAndDefaults = Javassist.unusedFields(clazz, f ->
                f.isReader() || isInitializer(f.where()) && isMemberOfClass(f.where(), clazz) && f.isWriter())
                .collect(Collectors.toMap(
                        Function.identity(),
                        (TFunction<CtField, Object>) f ->
                                Types.defaults(f.getType().toClass())));

        clazz.instrument(new ExprEditor() {
            @Override
            public void edit(FieldAccess f) {
                try {
                    Optional.ofNullable(fieldsAndDefaults.get(f.getField()))
                            .ifPresent((TConsumer<Object>) v ->
                                    f.replace("$_ = " + v));
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        return null;
    }

}
