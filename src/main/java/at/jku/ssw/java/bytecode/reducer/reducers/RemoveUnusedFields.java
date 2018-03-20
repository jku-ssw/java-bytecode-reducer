package at.jku.ssw.java.bytecode.reducer.reducers;

import javassist.CtClass;
import javassist.NotFoundException;

import java.util.Arrays;

public class RemoveUnusedFields implements Reducer {
    public CtClass transform(CtClass clazz) {
        Arrays.stream(clazz.getDeclaredFields())
                .forEach(f -> {
                    try {
                        clazz.removeField(f);
                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    }
                });
        return clazz;
    }
}
