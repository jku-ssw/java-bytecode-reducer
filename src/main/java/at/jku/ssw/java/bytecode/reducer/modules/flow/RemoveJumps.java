package at.jku.ssw.java.bytecode.reducer.modules.flow;

import at.jku.ssw.java.bytecode.reducer.context.Reduction;
import at.jku.ssw.java.bytecode.reducer.runtypes.RepeatableReducer;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist;
import javassist.CtClass;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;

import java.util.Arrays;

// TODO
public class RemoveJumps implements RepeatableReducer<Object> {
    @Override
    public Reduction.Result<Object> apply(Reduction.Base<Object> base) throws Exception {
        CtClass clazz = Javassist.loadClass(base.bytecode());

        Arrays.stream(clazz.getDeclaredMethods()).forEach(m -> {
            try {
                final MethodInfo    mi = m.getMethodInfo();
                final CodeAttribute ca = mi.getCodeAttribute();
                final CodeIterator  it = ca.iterator();

                while (it.hasNext()) {
                    final int index = it.next();
                    final int code  = it.byteAt(index);

//                    if (code == Opcode.TABLESWITCH)
                }
            } catch (BadBytecode badBytecode) {
                badBytecode.printStackTrace();
            }
        });

        return null;
    }
}
