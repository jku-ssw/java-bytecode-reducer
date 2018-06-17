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
                    var ca = m.getCodeAttribute();
                    var it = ca.iterator();

                    // index of the first instruction in a potentially
                    // removable sequence
                    int begin = -1;

                    // index of the first instruction after a potentially
                    // removable sequence
                    int end = -1;

                    // the current number of items on the stack
                    var stackSize = 0;

                    /*
                        Every constructor's code must begin with
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

                        // store the first index anyway
                        if (begin == -1)
                            begin = index;

                        // get the opcode at the current index position
                        int code = it.byteAt(index);

                        /*
                        If a "special" instruction is found, the range is
                        reset.
                        This should prevent control flow instructions from
                        being removed (e.g. sets of instructions that lead
                        to and include a conditional jump).
                        */
                        if (Code.isSpecial(code)) {
                            begin = -1;
                        }

                        var oldStackSize = stackSize;

                        stackSize = Code.getStackLevelChange(code);

                        logger.debug(
                                "%d: %-40s // Stack: %d -> %d",
                                index,
                                Mnemonic.OPCODE[code],
                                oldStackSize,
                                stackSize
                        );

                        // if the stack is empty (again), the instruction
                        // sequence
                        if (stackSize == 0) {

                        }
                    }

                    return new CodePosition("", 0, 0);
                })
                .filter(cp -> !base.cache().contains(cp))
                .findAny()
                .map(cp -> base.toResult(base.bytecode(), cp))
                .orElse(base.toMinimalResult());

    }

}
