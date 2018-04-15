package at.jku.ssw.java.bytecode.reducer.modules;

import at.jku.ssw.java.bytecode.reducer.context.Reduction.Base;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Result;
import at.jku.ssw.java.bytecode.reducer.runtypes.RepeatableReducer;
import at.jku.ssw.java.bytecode.reducer.utils.TConsumer;
import at.jku.ssw.java.bytecode.reducer.utils.TFunction;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.reducer.utils.Javassist.*;

public class RemoveUnusedFields implements RepeatableReducer<CtField> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Result<CtField> apply(Base<CtField> base) throws Exception {

        CtClass clazz = loadClass(base.bytecode());

        // get the first unused field that was not already attempted
        Optional<CtField> optField = unusedFields(clazz)
                .filter(f -> !base.cache().contains(f))
                .findFirst();

        // if no unused field was found, the reduction is minimal
        return optField.map((TFunction<CtField, Result<CtField>>) f ->
                base.toResult(removeField(clazz, f).toBytecode(), f))
                .orElse(base.toMinimalResult());
    }

    private Stream<CtField> unusedFields(CtClass clazz) throws CannotCompileException {
        FieldAccessVisitor visitor = new FieldAccessVisitor(clazz);

        clazz.instrument(visitor);

        return visitor.unusedFields.stream();
    }

    @Override
    public byte[] apply(byte[] bytecode) throws Exception {
        CtClass clazz = loadClass(bytecode);

        unusedFields(clazz).forEach(
                (TConsumer<CtField>) f -> removeField(clazz, f));

        return clazz.toBytecode();
    }

    private CtClass removeField(CtClass clazz, CtField field) throws NotFoundException {
        logger.debug("Removing field '{}'", field.getSignature());
        clazz.removeField(field);
        return clazz;
    }

    private class FieldAccessVisitor extends ExprEditor {
        private final Set<CtField> unusedFields;
        private final CtClass      clazz;

        FieldAccessVisitor(CtClass clazz) {
            this.clazz = clazz;
            this.unusedFields = Set.of(clazz.getDeclaredFields());
        }

        @Override
        public void edit(FieldAccess f) {
            // allow initial value declaration in constructor
            CtBehavior loc = f.where();

            if (isInitializer(loc) && isMemberOfClass(loc, clazz) && f.isWriter())
                return;

            try {
                CtField field = f.getField();

                unusedFields.remove(field);
            } catch (NotFoundException e) {
                // should not happen
                e.printStackTrace();
            }
        }
    }
}
