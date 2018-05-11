package at.jku.ssw.java.bytecode.reducer.runtypes;

import at.jku.ssw.java.bytecode.reducer.context.Reduction;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Base;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Result;
import at.jku.ssw.java.bytecode.reducer.utils.TConsumer;
import at.jku.ssw.java.bytecode.reducer.utils.TFunction;

import java.util.Optional;
import java.util.stream.Stream;

public interface FieldReducer<CLASS, FIELD>
        extends RepeatableReducer<FIELD> {

    default Result<FIELD> apply(Base<FIELD> base) throws Exception {

        CLASS clazz = classFrom(base.bytecode());

        // get the first applicable field that was not already attempted
        Optional<FIELD> optField = eligibleFields(clazz)
                .filter(f -> !base.cache().contains(f))
                .findFirst();

        // if no applicable field was found, the reduction is minimal
        return optField.map((TFunction<FIELD, Result<FIELD>>) f ->
                base.toResult(bytecodeFrom(handleField(clazz, f)), f))
                .orElse(base.toMinimalResult());
    }

    default byte[] apply(byte[] bytecode) throws Exception {
        CLASS clazz = classFrom(bytecode);

        eligibleFields(clazz).forEach(
                (TConsumer<FIELD>) f -> handleField(clazz, f));

        return bytecodeFrom(clazz);
    }

    default Result<FIELD> force(byte[] bytecode) throws Exception {
        CLASS clazz = classFrom(bytecode);

        Base<FIELD> base = Reduction.of(
                bytecodeFrom(
                        eligibleFields(clazz)
                                .map(f -> (TFunction<CLASS, CLASS>) c -> handleField(c, f))
                                .reduce(c -> c, (f1, f2) -> c -> f2.apply(f1.apply(c)))
                                .apply(clazz)));

        return base.toMinimalResult();
    }

    CLASS classFrom(byte[] bytecode) throws Exception;

    byte[] bytecodeFrom(CLASS clazz) throws Exception;

    Stream<FIELD> eligibleFields(CLASS clazz) throws Exception;

    CLASS handleField(CLASS clazz, FIELD field) throws Exception;
}
