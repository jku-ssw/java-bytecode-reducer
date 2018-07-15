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
import javassist.bytecode.Mnemonic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static javassist.bytecode.Opcode.NOP;

/**
 * Removes sequences of instructions that are neutral to the stack
 * (e.g. delete as many stack-pushes as stack-pops).
 */
@Expensive(heaviness = 10)
@Unsound
public class RemoveInstructionSequences implements InstructionReducer {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public CtClass reduce(CtClass clazz,
                          CtBehavior behav,
                          CodePosition codePosition,
                          CodeIterator it) {

        var begin = codePosition.begin;
        var end   = codePosition.end;

        logger.debug(
                "Removing instructions of behaviour '{}' from index {} to {}",
                behav.getLongName(),
                begin,
                end
        );

        // replace the determined instruction range with NOPs
        IntStream.range(begin, end)
                .forEach(i -> it.writeByte(NOP, i));

        return clazz;
    }

    @Override
    public Stream<CodePosition> codePositions(CtBehavior method, CodeIterator it) throws BadBytecode {
        var name = method.getLongName();

        logger.trace(name);

        // store the markings at which the stack size is zero
        // and that are not NOPs
        var beginIndices = new ArrayList<Integer>();

        // store the markings at which the stack size is zero
        // which may be NOPs
        var endIndices = new ArrayList<Integer>();

        // the current number of items on the stack
        var stackSize = 0;

        while (it.hasNext()) {
            int index = it.next();

            // get the opcode at the current index position
            int code = it.byteAt(index);

            // If the stack size is zero BEFORE the current
            // instruction, either a previous sequence was
            // discarded or the loop just started
            if (stackSize == 0 && code != NOP)
                beginIndices.add(index);

            var oldStackSize = stackSize;

            // calculate the new stack level
            stackSize = Code.newStackLevel(
                    method,
                    oldStackSize,
                    code,
                    index,
                    it
            );

            logger.trace(String.format(
                    "%6d: %-40s // Stack: %d -> %d",
                    index,
                    Mnemonic.OPCODE[code],
                    oldStackSize,
                    stackSize
            ));

            // if the stack is empty (again), remember the next index
            // as this may be a potential end of a removable sequence
            if (stackSize == 0)
                endIndices.add(it.lookAhead());
        }

        // perform sorting before return expression
        endIndices.sort(Comparator.reverseOrder());

        return beginIndices.stream()
                .flatMap(i ->
                        endIndices.stream()
                                .filter(j -> j > i)
                                .map(j -> new CodePosition(name, i, j))
                );
    }

}
