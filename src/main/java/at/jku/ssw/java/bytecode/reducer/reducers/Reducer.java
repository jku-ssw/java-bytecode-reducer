package at.jku.ssw.java.bytecode.reducer.reducers;

import javassist.CtClass;

public interface Reducer {
    CtClass transform(CtClass clazz);
}
