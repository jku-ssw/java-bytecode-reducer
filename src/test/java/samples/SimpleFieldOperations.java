package samples;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.InstructionAdapter;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

public class SimpleFieldOperations extends BytecodeSample {

    public static Bytecode bytecode(int version) {
        var instance = new SimpleFieldOperations();
        return instance.new Bytecode(
                instance
                        .bar(instance.assemble(version))
                        .toByteArray()
        );
    }

    /*
    ACC_PUBLIC bar()V:
        ALOAD_0
        ALOAD_0
        GETFIELD #foo
        PUTFIELD #foo
     */
    private ClassWriter bar(ClassWriter cw) {
        var mv = new InstructionAdapter(cw.visitMethod(ACC_PUBLIC, "bar", "()V", null, null));
        mv.visitCode();
        mv.load(0, type);
        mv.load(0, type);
        mv.getfield(internalName, "foo", "I");
        mv.putfield(internalName, "foo", "I");
        mv.visitMaxs(2, 1);
        mv.visitEnd();
        return cw;
    }

    public static ClassWriter clazz(int version) {
        return new SimpleFieldOperations().assemble(version);
    }

    public static byte[] bar(int version) {
        var instance = new SimpleFieldOperations();
        var cw = instance.bar(instance.assemble(version));

        return cw.toByteArray();
    }

    @Override
    protected ClassWriter prepareFields(ClassWriter cw) {
        cw.visitField(ACC_PUBLIC, "foo", "I", null, null);
        return cw;
    }
}
