package at.jku.ssw.java.bytecode.reducer.runtypes;

import at.jku.ssw.java.bytecode.reducer.context.Reduction;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Base;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Result;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TFunction;

import java.util.Optional;
import java.util.stream.Stream;

public interface MemberReducer<CLASS, MEMBER>
        extends RepeatableReducer<MEMBER> {

    default Result<MEMBER> apply(Base<MEMBER> base) throws Exception {

        CLASS clazz = classFrom(base.bytecode());

        // get the first applicable member that was not already attempted
        Optional<MEMBER> optMember = getMembers(clazz)
                .filter(f -> !base.cache().contains(f))
                .findAny();

        // if no applicable member was found, the reduction is minimal
        return optMember.map((TFunction<MEMBER, Result<MEMBER>>) f ->
                base.toResult(bytecodeFrom(process(clazz, f)), f))
                .orElse(base.toMinimalResult());
    }

    default Result<MEMBER> force(byte[] bytecode) throws Exception {
        CLASS clazz = classFrom(bytecode);

        Base<MEMBER> base = Reduction.of(
                bytecodeFrom(
                        getMembers(clazz)
                                .map(f -> (TFunction<CLASS, CLASS>) c -> process(c, f))
                                .reduce(c -> c, (f1, f2) -> c -> f2.apply(f1.apply(c)))
                                .apply(clazz)));

        return base.toMinimalResult();
    }

    CLASS classFrom(byte[] bytecode) throws Exception;

    byte[] bytecodeFrom(CLASS clazz) throws Exception;

    Stream<MEMBER> getMembers(CLASS clazz) throws Exception;

    CLASS process(CLASS clazz, MEMBER MEMBER) throws Exception;
}
