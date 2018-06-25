package at.jku.ssw.java.bytecode.reducer.modules.misc;

import at.jku.ssw.java.bytecode.reducer.context.Reduction.Base;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Result;
import at.jku.ssw.java.bytecode.reducer.runtypes.RepeatableReducer;
import at.jku.ssw.java.bytecode.reducer.utils.CodePosition;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TFunction;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist;
import javassist.CtMethod;
import javassist.bytecode.Opcode;

import java.util.Arrays;


/**
 * Removes instruction sequences that result in an exception being thrown.
 * <p>
 * <p>
 * e.g.:
 * <p>
 * 0: new           #6                  // class java/lang/IllegalArgumentException
 * 3: dup
 * 4: invokespecial #7                  // Method java/lang/IllegalArgumentException."<init>":()V
 * 7: athrow
 * <p>
 * or
 * <p>
 * 0: new           #6                  // class java/lang/IllegalArgumentException
 * 3: dup
 * 4: ldc           #7                  // String test
 * 6: invokespecial #8                  // Method java/lang/IllegalArgumentException."<init>":(Ljava/lang/String;)V
 * 9: athrow
 */
public class RemoveThrowSequences implements RepeatableReducer<CodePosition> {

    @Override
    public Result<CodePosition> apply(Base<CodePosition> base) throws Exception {
        final var bytecode  = base.bytecode();
        final var clazz     = Javassist.loadClass(bytecode);
        final var constPool = clazz.getClassFile().getConstPool();

        return Arrays.stream(clazz.getDeclaredMethods())
                .map((TFunction<CtMethod, CodePosition>) method -> {
                    var m = method.getMethodInfo();

                    var ca = m.getCodeAttribute();
                    var it = ca.iterator();

                    while (it.hasNext()) {
                        var index = it.next();
                        var code  = it.byteAt(index);

                        if (code == Opcode.NEW) {
                            // operand for Opcode "new"
                            // is 2 byte index
                            var op1 = it.byteAt(index + 1);
                            var op2 = it.byteAt(index + 2);

                            // compute constant pool (=indexbyte1 << 8 + indexbyte2)
                            var cpIndex = (op1 << 8) + op2;
//                            var type    = constPool.getClassInfoByDescriptor()
                            System.out.println("Class info: " + constPool.getClassInfo(cpIndex));
                            System.out.println("Class info descriptor: " + constPool.getClassInfoByDescriptor(cpIndex));
                        }
                    }

                    return new CodePosition(method.getLongName(), 0, 0);
                })
                .findFirst()
                .map(cp -> base.toResult(bytecode, cp))
                .orElseGet(base::toMinimalResult);
    }
}
