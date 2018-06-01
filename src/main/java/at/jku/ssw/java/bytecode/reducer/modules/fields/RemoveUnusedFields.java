package at.jku.ssw.java.bytecode.reducer.modules.fields;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.runtypes.MemberReducer;
import at.jku.ssw.java.bytecode.reducer.utils.Javassist;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TConsumer;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TPredicate;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.expr.FieldAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.reducer.utils.Javassist.*;

@Sound
public class RemoveUnusedFields implements MemberReducer<CtClass, CtField> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public CtClass classFrom(byte[] bytecode) throws IOException {
        return loadClass(bytecode);
    }

    @Override
    public byte[] bytecodeFrom(CtClass clazz) throws IOException, CannotCompileException {
        return Javassist.bytecode(clazz);
    }

    @Override
    public Stream<CtField> getMembers(CtClass clazz) throws CannotCompileException {
        return unusedFields(clazz, f ->
                isInitializer(f.where()) && isMemberOfClass(f.where(), clazz) && f.isWriter());
    }

    @Override
    public CtClass process(CtClass clazz, CtField field) throws NotFoundException, CannotCompileException {
        logger.debug("Removing field '{}'", field.getSignature());

        // replaces field access in constructors with local variables
        Javassist.forFieldAccesses(
                clazz,
                (TPredicate<FieldAccess>) fa -> field.equals(fa.getField()),
                (TConsumer<FieldAccess>) f -> f.replace("")
        );

//        removeAccesses(clazz, field);

        clazz.removeField(field);

        return clazz;
    }

//    private void removeAccesses(CtClass clazz, CtField field) {
//        Arrays.stream(clazz.getDeclaredBehaviors()).forEach((TConsumer<CtBehavior>) m -> {
//            MethodInfo methodInfo = m.getMethodInfo();
//            ConstPool  constPool  = methodInfo.getConstPool();
//
//            CodeIterator it = methodInfo.getCodeAttribute().iterator();
//
//            while (it.hasNext()) {
//                int index = it.next();
//                int op    = it.byteAt(index);
//
//                // check object access
////                if (op == Opcode.ALOAD || op >= Opcode.ALOAD_0 && op <= Opcode.ALOAD_3) {
////                    for (int nextIndex = it.next(),
////                         nextOp = it.byteAt(nextIndex);
////                            it.hasNext() && nextOp != Opcode.ASTORE && (nextOp < Opcode.ASTORE_0 || nextOp > Opcode.ASTORE_3);
////                            nextIndex = it.next(),
////                            nextOp = it.byteAt(nextIndex)){
//                if (op == Opcode.PUTFIELD) {
//                    int    fieldIndex = it.u16bitAt(index + 1);
//                    String name       = constPool.getFieldrefName(fieldIndex);
//
//                    if (field.getName().equals(name)) {
//                        System.out.println("PUTFIELD " + name);
//                        it.writeByte(Opcode.NOP, index);
//                        it.writeByte(Opcode.NOP, index + 1);
//                        it.writeByte(Opcode.NOP, index + 2);
//                    }
//                }
//            }
//        });
//    }
}
