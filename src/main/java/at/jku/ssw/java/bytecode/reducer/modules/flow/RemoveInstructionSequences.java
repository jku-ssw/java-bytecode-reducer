package at.jku.ssw.java.bytecode.reducer.modules.flow;

import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.context.Reduction;
import at.jku.ssw.java.bytecode.reducer.runtypes.RepeatableReducer;
import at.jku.ssw.java.bytecode.reducer.utils.CodePosition;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TFunction;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Code;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist;
import javassist.CtBehavior;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Mnemonic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Objects;

import static javassist.bytecode.Opcode.NOP;

/**
 * Removes sequences of instructions that are neutral to the stack
 * (e.g. delete as many stack-pushs as stack-pops).
 */
@Unsound
public class RemoveInstructionSequences implements RepeatableReducer<CodePosition> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Reduction.Result<CodePosition> apply(Reduction.Base<CodePosition> base) throws Exception {
        final var clazz = Javassist.loadClass(base.bytecode());
        return Arrays.stream(clazz.getDeclaredMethods())
                .map(CtBehavior::getMethodInfo)
                .map((TFunction<MethodInfo, CodePosition>) m -> {
                    final var methodName = m.getName();

                    logger.debug(methodName);

                    var ca = m.getCodeAttribute();
                    var it = ca.iterator();

                    // index of the first instruction in a potentially
                    // removable sequence
                    int begin = -1;

                    // the current number of items on the stack
                    var stackSize = 0;

                    // flag to check whether the current instructions
                    // are within a potentially removable sequence
                    var seqDetected = false;

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
                            begin = index;
                        }

                        /*
                            If a "special" instruction is found, the range is
                            reset.
                            This should prevent control flow instructions from
                            being removed (e.g. sets of instructions that lead
                            to and include a conditional jump).
                        */

                        var oldStackSize = stackSize;

                        stackSize += Code.getStackLevelChange(code);

                        logger.debug(String.format(
                                "%d: %-40s // Stack: %d -> %d",
                                index,
                                Mnemonic.OPCODE[code],
                                oldStackSize,
                                stackSize
                        ));

                        // reset stack size and range start in
                        // case of "special" instructions
                        if (Code.isSpecial(code)) {
                            begin = -1;
                            stackSize = 0;
                        } else {

                            // if the stack is empty (again), the instruction
                            // sequence may be valid
                            if (stackSize == 0 && begin != -1) {
                                var cp = new CodePosition(methodName, begin, index);

                                if (!base.cache().contains(cp)) {
                                    // IntStream.range()

                                    // TODO determine
                                    return cp;
                                }

                                begin = -1;
                            }
                        }
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .filter(cp -> !base.cache().contains(cp))
                .findAny()
                .map(cp -> base.toMinimalResult())
                .orElse(null);
//                .map(cp -> base.toResult(base.bytecode(), cp))
//                .orElse(base.toMinimalResult());

    }

}
