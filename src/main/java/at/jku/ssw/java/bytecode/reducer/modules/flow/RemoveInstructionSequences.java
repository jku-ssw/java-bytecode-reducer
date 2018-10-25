package at.jku.ssw.java.bytecode.reducer.modules.flow;

import at.jku.ssw.java.bytecode.reducer.annot.Expensive;
import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.states.Reduction;
import at.jku.ssw.java.bytecode.reducer.runtypes.InstructionReducer;
import at.jku.ssw.java.bytecode.reducer.utils.cachetypes.CodePosition;
import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.analysis.Frame;
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
                                             CodeIterator it,
                                             Frame[] frames) throws BadBytecode {
        var name = method.getLongName();

        logger.trace(name);

        // store the markings at which the stack size is zero
        // and that are not NOPs
        var beginIndices = new ArrayList<Integer>();

        // the current number of items on the stack
        // (initialize with -1 as first iteration
        var stackSize = -1;

        while (it.hasNext()) {
            int index = it.next();

            // get the opcode at the current index position
            int code = it.byteAt(index);

            // get the execution frame at this index position
            var frame = frames[index];
            // since the top index points to the position, the actual
            // length is computed by adding 1
            // if there is no frame, this means that the instruction is
            // unreachable (dead code) and can usually be included in any
            // reduction
            stackSize = frame != null ? frame.getTopIndex() + 1 : 0;

            logger.trace(String.format(
                    "%6d: %-20s // [ %d ]",
                    index,
                    Mnemonic.OPCODE[code],
                    stackSize
            ));

            if (stackSize == 0) {

                if (code != NOP) {
                    // if the stack size at this instruction is zero
                    // and it is not a NOP, it is the start of a probably removable
                    // range
                    beginIndices.add(index);

                    // if the stack is empty, this index may also be the end
                    // of a potentially removable instruction sequence
                    var opt = beginIndices.stream()
                            .filter(i -> i < index)
                            .map(i -> new CodePosition(name, i, index))
                            .filter(base::isNotCached)
                            .findAny()
                            .map(cp -> reduce(method, cp, it));

                    if (opt.isPresent())
                        return opt;
                }
            }

        }

        return Optional.empty();
    }

}
