package at.jku.ssw.java.bytecode.reducer.modules.flow;

import at.jku.ssw.java.bytecode.reducer.annot.Expensive;
import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.context.Reduction;
import at.jku.ssw.java.bytecode.reducer.runtypes.InstructionReducer;
import at.jku.ssw.java.bytecode.reducer.utils.cachetypes.CodePosition;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Code;
import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Mnemonic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.IntStream;

import static javassist.bytecode.Opcode.NOP;

/**
 * Removes sequences of instructions that are neutral to the stack
 * (e.g. delete as many stack-pushes as stack-pops).
 */
@Expensive
@Unsound
public class RemoveInstructionSequences implements InstructionReducer {

    private static final Logger logger = LogManager.getLogger();

    public CodePosition reduce(CtBehavior behav,
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

        return codePosition;
    }

    @Override
    public Optional<CodePosition> reduceNext(Reduction.Base<CodePosition> base,
                                             CtBehavior method,
                                             CodeIterator it) throws BadBytecode {
        var name = method.getLongName();

        logger.trace(name);

        // store the markings at which the stack size is zero
        // and that are not NOPs
        var beginIndices = new ArrayList<Integer>();

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

            // if the stack is empty (again), the next index may be the end
            // of a potentially removable instruction sequence

            if (stackSize == 0) {
                var end = it.lookAhead();
                // potentially removable code position
                var opt = beginIndices.stream()
                        .map(begin -> new CodePosition(name, begin, end))
                        .filter(base::isNotCached)
                        .findAny()
                        .map(cp -> reduce(method, cp, it));

                if (opt.isPresent())
                    return opt;
            }
        }

        return Optional.empty();
    }

}
