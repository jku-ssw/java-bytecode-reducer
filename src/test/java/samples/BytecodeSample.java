package samples;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

public abstract class BytecodeSample {

    public static final String DEFAULT_SUPERCLASS = "java/lang/Object";

    public final String className;
    public final String internalName;
    public final Type type;

    protected BytecodeSample() {
        className = getClass().getSimpleName();
        internalName = Type.getInternalName(getClass());
        type = Type.getType(getClass());
    }

    protected ClassWriter assemble(int version) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(version, ACC_PUBLIC, className, null, DEFAULT_SUPERCLASS, new String[]{});
        return prepareFields(cw);
    }

    protected abstract ClassWriter prepareFields(ClassWriter cw);
}
