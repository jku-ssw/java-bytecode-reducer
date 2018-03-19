package at.jku.ssw.java.bytecode.reducer.reducers;

import javassist.CtClass;

public class RemoveUnusedFields implements Reducer {
    public CtClass transform(CtClass clazz) {
//        return clazz.getDeclaredFields()
        return clazz;
    }
}
