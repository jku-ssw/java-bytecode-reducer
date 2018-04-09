package at.jku.ssw.java.bytecode.reducer.runtypes;

import javassist.CtClass;

public interface Reducer {
    CtClass transform(CtClass clazz) throws Exception;
}
