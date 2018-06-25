package at.jku.ssw.java.bytecode.reducer.modules.flow;

import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Base;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Result;
import at.jku.ssw.java.bytecode.reducer.runtypes.RepeatableReducer;
import at.jku.ssw.java.bytecode.reducer.utils.CodePosition;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TFunction;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Code;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Mnemonic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import static javassist.bytecode.Opcode.NOP;

/**
 * Removes sequences of instructions that are neutral to the stack
 * (e.g. delete as many stack-pushs as stack-pops).
 */
@Unsound
public class RemoveInstructionSequences implements RepeatableReducer<CodePosition> {

    private static final Logger logger = LogManager.getLogger();

    /**
     * Reduces the given class at the given code position by
     * replacing all instructions withing the given range by NOPs.
     *
     * @param base         The reduction base
     * @param clazz        The class to reduce
     * @param codePosition The determined code position
     * @return the reduction result
     */
    private Result<CodePosition> process(Base<CodePosition> base,
                                         ClassFile clazz,
                                         CodePosition codePosition) {

        var ca = codePosition.method.getCodeAttribute();
        var it = ca.iterator();

        var begin = codePosition.begin;
        var end   = codePosition.end;

        logger.info(
                "Removing instructions of method '{}' from index {} to {}",
                codePosition.method.getName(),
                begin,
                end
        );

        // replace the determined byte range with NOPs
        IntStream.range(begin, end)
                .forEach(i -> it.writeByte(NOP, i));

        clazz.compact();

        try {
            return base.toResult(Javassist.bytecode(clazz), codePosition);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Result<CodePosition> apply(Base<CodePosition> base) throws Exception {
        final var clazz = Javassist.loadClass(base.bytecode()).getClassFile();

        @SuppressWarnings("unchecked") final var methods = (List<MethodInfo>) clazz.getMethods();

        return methods.stream()
                .map((TFunction<MethodInfo, Optional<CodePosition>>) m -> {
                    logger.trace(m.getName() + m.getDescriptor());

                    final var ca = m.getCodeAttribute();
                    final var it = ca.iterator();

                    // store the markings at which the stack size is zero
                    // and that are not NOPs
                    var beginIndices = new ArrayList<Integer>();

                    // store the markings at which the stack size is zero
                    // which may be NOPs
                    var endIndices = new ArrayList<Integer>();

                    // the current number of items on the stack
                    var stackSize = 0;

                    /*
                        Every constructor code begins with
                        a call to the initialization method:
                        aload_0
                        invokespecial #1

                        In case of a constructor method, those first
                        two instructions are therefore skipped,
                        as the stack is again empty after this sequence.
                    */
                    it.skipConstructor();

                    while (it.hasNext()) {
                        int index = it.next();

                        // get the opcode at the current index position
                        int code = it.byteAt(index);

                        // If the stacksize is zero BEFORE the current
                        // instruction, either a previous sequence was
                        // discarded or the loop just started
                        if (stackSize == 0 && code != NOP) {
                            beginIndices.add(index);
                        }

                        var oldStackSize = stackSize;

                        // calculate the new stack level
                        stackSize = Code.newStackLevel(oldStackSize, code, index, it);

                        logger.trace(String.format(
                                "%d: %-40s // Stack: %d -> %d",
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

                    return beginIndices.stream()
                            .flatMap(i ->
                                    endIndices.stream()
                                            .filter(j -> j > i)
                                            .map(j -> new CodePosition(m, i, j))
                            )
                            .filter(cp -> !base.cache().contains(cp))
                            .findAny();
                })
                .filter(Optional::isPresent)
                .findAny()
                .flatMap(Function.identity())
                .map(cp -> process(base, clazz, cp))
                .orElse(base.toMinimalResult());
    }

}
