package at.jku.ssw.java.bytecode.reducer.context;

import at.jku.ssw.java.bytecode.reducer.modules.cleanup.ShrinkConstantPool;
import at.jku.ssw.java.bytecode.reducer.modules.fields.*;
import at.jku.ssw.java.bytecode.reducer.modules.flow.RemoveConstantAssignments;
import at.jku.ssw.java.bytecode.reducer.modules.flow.RemoveInstructionSequences;
import at.jku.ssw.java.bytecode.reducer.modules.flow.RemoveNeutralInstructions;
import at.jku.ssw.java.bytecode.reducer.modules.methods.*;
import at.jku.ssw.java.bytecode.reducer.runtypes.Reducer;

import java.util.List;

/**
 * Holds all available modules and offers API to query them.
 */
public final class ModuleRegistry {
    private ModuleRegistry() {
    }

    /**
     * Returns a collection of all registered modules.
     *
     * @return a list containing all reducer classes
     */
    public static List<Class<? extends Reducer>> allModules() {
        return List.of(
                RemoveUnusedFields.class,
                RemoveUnusedMethods.class,
                RemoveWriteOnlyFields.class,
                RemoveEmptyMethods.class,
                RemoveReadOnlyFields.class,
                RemoveStaticFieldAttributes.class,
                RemoveAllFieldAttributes.class,
                RemoveAllMethodAttributes.class,
                RemoveConstantAssignments.class,
                RemoveNeutralInstructions.class,
                ShrinkConstantPool.class,
                RemoveMethodAttributes.class,
                RemoveFieldAttributes.class,
                RemoveInitializers.class,
                ReplaceMethodCalls.class,
                RemoveVoidMethodCalls.class,
                RemoveInstructionSequences.class
        );
    }
}