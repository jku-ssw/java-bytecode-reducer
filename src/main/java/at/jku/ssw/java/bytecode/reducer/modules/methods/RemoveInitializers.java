package at.jku.ssw.java.bytecode.reducer.modules.methods;

import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.utils.functional.Catch;
import at.jku.ssw.java.bytecode.reducer.runtypes.JavassistHelper;
import at.jku.ssw.java.bytecode.reducer.runtypes.MemberReducer;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Expressions;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Instrumentation;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Members;
import javassist.CtClass;
import javassist.CtConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Randomly removes initializers (constructors, static initializers) from
 * the given class.
 */
@Unsound
public class RemoveInitializers
        implements MemberReducer<CtClass, CtConstructor, String>, JavassistHelper {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Stream<CtConstructor> getMembers(CtClass clazz) {
        return Arrays.stream(clazz.getDeclaredConstructors())
                // only select non-default initializers
                .filter(c -> !c.isEmpty());
    }

    @Override
    public String keyFromMember(CtConstructor constructor) {
        return constructor.getLongName();
    }

    @Override
    public CtClass process(CtClass clazz, CtConstructor constructor) throws Exception {
        logger.debug("Removing constructor '{}'", constructor.getLongName());

        // replace each call to the given constructor
        Instrumentation.forNewExpressions(
                clazz,
                Catch.predicate(e -> e.getConstructor().equals(constructor)),
                Catch.consumer(e -> e.replace(Expressions.replaceAssign("$0"))));

        // replace each object assignment that is performed
        // by invoking this constructor
        Instrumentation.forConstructorCalls(
                clazz,
                Catch.predicate(c -> c.getConstructor().equals(constructor)),
                Catch.consumer(c -> c.replace(Expressions.NO_EXPRESSION)));

        // clear constructor
        constructor.setBody(null);

        // and revoke modifiers
        constructor.setModifiers(Members.Attribute.NONE);

        /*
        Leave it be if it is the default constructor
        (as per default every class file has a default constructor implicitly
        supplied by the compiler).
        As the CtConstructor#isEmpty method also returns true for
        parameterized constructors, also ensure that there are none.
        */
        if (!(constructor.isEmpty() && constructor.getParameterTypes().length == 0))
            clazz.removeConstructor(constructor);

        return clazz;
    }
}
