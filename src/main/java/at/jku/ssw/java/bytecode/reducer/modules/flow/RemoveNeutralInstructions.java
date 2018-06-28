package at.jku.ssw.java.bytecode.reducer.modules.flow;

import at.jku.ssw.java.bytecode.reducer.annot.Expensive;
import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.runtypes.InstructionReducer;
import at.jku.ssw.java.bytecode.reducer.utils.CodePosition;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Code;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Opcode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static javassist.bytecode.Opcode.NOP;

/**
 * Removes single instructions that are neutral to the stack level.
 */
@Expensive
@Unsound
public class RemoveNeutralInstructions implements InstructionReducer {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public CtClass reduce(CtClass clazz, CtBehavior behav, CodePosition codePosition, CodeIterator iterator) {
        var begin = codePosition.begin;
        var end   = codePosition.end;

        logger.debug(
                "Removing stack-neutral instruction at index {} of behaviour '{}'",
                begin,
                behav.getLongName()
        );

        // replace the determined instruction range with NOPs
        IntStream.range(begin, end)
                .forEach(i -> iterator.writeByte(NOP, i));

        return clazz;
    }

    @Override
    public Stream<CodePosition> codePositions(CtClass clazz, CtBehavior method, CodeIterator it) throws BadBytecode {
        var name      = method.getLongName();
        var positions = new ArrayList<CodePosition>();

        while (it.hasNext()) {
            int begin = it.next();
            // get the opcode at the current index position
            int code = it.byteAt(begin);

            // skip NOPs
            if (code == Opcode.NOP)
                continue;

            // the end index is either the next instruction
            // or simply plus 1 for return statements
            var end = it.hasNext() ? it.lookAhead() : begin + 1;

            // calculate the change in stack level that the
            // current operation forces
            int change = Code.getStackLevelChange(
                    method,
                    code,
                    begin,
                    it
            );

            // if potential site is found, store it
            if (change == 0)
                positions.add(new CodePosition(name, begin, end));
        }

        return positions.stream();
    }

}
