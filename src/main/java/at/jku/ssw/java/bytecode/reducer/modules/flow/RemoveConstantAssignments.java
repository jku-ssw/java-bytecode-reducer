package at.jku.ssw.java.bytecode.reducer.modules.flow;

import at.jku.ssw.java.bytecode.reducer.annot.Expensive;
import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.runtypes.InstructionReducer;
import at.jku.ssw.java.bytecode.reducer.utils.CodePosition;
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
import static javassist.bytecode.Opcode.SASTORE;

/**
 * Removes consecutive constant assignments.
 * e.g.
 * ICONST_0
 * ILOAD_0
 */
@Expensive
@Unsound
public class RemoveConstantAssignments implements InstructionReducer {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public CtClass reduce(CtClass clazz, CtBehavior behav, CodePosition codePosition, CodeIterator it) {
        var begin = codePosition.begin;
        var end   = codePosition.end;

        logger.debug(
                "Removing constant assignment at indices {} to {} of behaviour '{}'",
                begin,
                end,
                behav.getLongName()
        );

        // replace the determined instruction range with NOPs
        IntStream.range(begin, end)
                .forEach(i -> it.writeByte(NOP, i));

        return clazz;
    }

    @Override
    public Stream<CodePosition> codePositions(CtBehavior method, CodeIterator it) throws BadBytecode {
        var name = method.getLongName();

        var begin = -1;

        var positions = new ArrayList<CodePosition>();

        while (it.hasNext()) {
            int index = it.next();
            // get the opcode at the current index position
            int code = it.byteAt(index);

            // Check that instruction has successor
            // and that current instruction is something
            // like CONST_* or LDC_*
            if (it.hasNext() && (
                    Opcode.ACONST_NULL <= code
                            && code <= Opcode.DCONST_1
                            || Opcode.LDC <= code
                            && code <= Opcode.LDC2_W)
                    ) {
                // then remember for next iteration
                begin = index;
            } else if (begin != -1 && (
                    Opcode.ISTORE <= code && code <= SASTORE
                            || code == Opcode.POP)
                    ) {
                int end = it.hasNext() ? it.lookAhead() : index + 1;

                positions.add(new CodePosition(name, begin, end));
            } else {
                // otherwise reset flag
                begin = -1;
            }
        }

        return positions.stream();
    }

}
