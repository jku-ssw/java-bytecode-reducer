package at.jku.ssw.java.bytecode.reducer.reducers;

import javassist.CtClass;

public abstract class Reducer {
    protected Reducer() {
    }

    public abstract void transform(CtClass clazz);

    @Override
    public String toString() {
        // TODO maybe improve (e.g. degree of complexity etc.)
        return getClass().getSimpleName();
    }
}
