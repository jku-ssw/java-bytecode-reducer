package at.jku.ssw.java.bytecode.reducer.modules;

import at.jku.ssw.java.bytecode.reducer.runtypes.Reducer;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RemoveUnusedFields implements Reducer {
    private static final Logger logger = LogManager.getLogger();

    public CtClass transform(CtClass clazz) throws Exception {
        Set<CtField> fields = new HashSet<>(Arrays.asList(clazz.getDeclaredFields()));

        clazz.instrument(new ExprEditor() {

            @Override
            public void edit(FieldAccess fieldAccess) {
                // allow initial value declaration in constructor
                if (isInitialization(clazz, fieldAccess))
                    return;

                try {
                    CtField field = fieldAccess.getField();

                    fields.remove(field);
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        fields.forEach(f -> {
            try {
                logger.debug("Removing field '{} {}'", f.getType().getName(), f.getName());
                clazz.removeField(f);
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        });

        return clazz;
    }

    static boolean inClass(CtClass clazz, CtMember member) {
        return clazz.equals(member.getDeclaringClass());
    }

    static boolean isInitialization(CtClass declaringClass, FieldAccess access) {
        CtBehavior location = access.where();

        return location instanceof CtConstructor &&
                inClass(declaringClass, location) &&
                access.isWriter();
    }

    private static class FieldAccessVisitor extends ExprEditor {
        private final Set<CtField> accessedFields = new HashSet<>();

        @Override
        public void edit(FieldAccess f) throws CannotCompileException {
            super.edit(f);
        }
    }
}
