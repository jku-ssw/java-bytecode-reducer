package at.jku.ssw.java.bytecode.reducer.modules.preprocessing;

import at.jku.ssw.java.bytecode.reducer.runtypes.Reducer;
import org.junit.jupiter.api.Assertions;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

abstract class ReducerTest {
    protected static <T extends Reducer> ReducerAssertion<T> assertThat(Class<T> type) {
        return new ReducerAssertion<>(type);
    }

    protected static class ReducerAssertion<T extends Reducer> {
        private final Class<T> type;

        private ReducerAssertion(Class<T> type) {
            this.type = type;
        }

        protected ResultAssertion<T> reduces(byte[] bytecode) {
            return new ResultAssertion<>(type, bytecode);
        }
    }

    protected static class ResultAssertion<T extends Reducer> {
        private final Class<T> type;
        private final byte[] bytecode;


        private ResultAssertion(Class<T> type, byte[] bytecode) {
            this.type = type;
            this.bytecode = bytecode;
        }

        protected void to(byte[] expected) throws Exception {
            T reducer;
            try {
                reducer = type.getConstructor().newInstance();
            } catch (Exception e) {
                throw new AssertionError("Could not instantiate reducer", e);
            }

            byte[] actual = reducer.apply(bytecode);
            assertEqualClassStructure(expected, actual);
        }
    }

    static void assertEqualClassStructure(byte[] expected, byte[] actual) {
        ClassReader rEx = new ClassReader(expected);
        ClassReader rAc = new ClassReader(actual);

        assertEquals(rEx.getAccess(), rAc.getAccess(), "Class access modifiers");
        assertEquals(rEx.getClassName(), rAc.getClassName(), "Class names");
        assertEquals(rEx.getItemCount(), rAc.getItemCount(), "Constant pool size");
        assertEquals(rEx.getSuperName(), rAc.getSuperName(), "Superclass");

        assertArrayEquals(
                rEx.getInterfaces(),
                rAc.getInterfaces(),
                Assertions::assertEquals,
                String::compareTo
        );

        ClassNode nEx = new ClassNode();
        ClassNode nAc = new ClassNode();
        rEx.accept(nEx, 0);
        rAc.accept(nAc, 0);

        assertListEquals(
                nEx.fields,
                nAc.fields,
                (fEx, fAc) -> {
                    assertEquals(fEx.name, fAc.name);
                    assertEquals(fEx.access, fAc.access);
                    assertEquals(fEx.signature, fAc.signature);
                    assertEquals(fEx.value, fAc.value);
                    // TODO maybe compare annotations
                },
                Comparator.comparing(f -> f.name)
        );

        assertListEquals(
                nEx.methods,
                nAc.methods,
                (mEx, mAc) -> {
                    assertEquals(mEx.name, mAc.name);
                    assertEquals(mEx.access, mAc.access);
                    assertEquals(mEx.signature, mAc.signature);
                    assertEquals(mEx.maxLocals, mAc.maxLocals);
                    assertEquals(mEx.maxStack, mAc.maxStack);

                    assertListEquals(
                            mEx.parameters,
                            mAc.parameters,
                            (pEx, pAc) -> {
                                assertEquals(pEx.access, pAc.access);
                                assertEquals(pEx.name, pAc.name);
                            }
                    );

                    assertListEquals(
                            mEx.exceptions,
                            mAc.exceptions,
                            Assertions::assertEquals,
                            String::compareTo
                    );

                    assertArrayEquals(
                            mEx.instructions.toArray(),
                            mAc.instructions.toArray(),
                            (iEx, iAc) -> {
                                assertEquals(iEx.getOpcode(), iAc.getOpcode());
                                assertEquals(iEx.getType(), iAc.getType());
                            }
                    );
                },
                Comparator.comparing(m -> m.name + m.signature)
        );
    }

    static <T> void assertListEquals(List<T> expected, List<T> actual, BiConsumer<T, T> assertions) {
        assertListEquals(expected, actual, assertions, null);
    }

    @SuppressWarnings("unchecked")
    static <T> void assertListEquals(List<T> expected, List<T> actual, BiConsumer<T, T> assertions, Comparator<T> cmp) {
        if (expected == null)
            assertNull(actual);
        else {
            assertNotNull(actual);

            assertArrayEquals(
                    (T[]) expected.toArray(),
                    (T[]) actual.toArray(),
                    assertions,
                    cmp
            );
        }
    }

    static <T> void assertArrayEquals(T[] expected, T[] actual, BiConsumer<T, T> assertions) {
        assertArrayEquals(expected, actual, assertions, null);
    }

    static <T> void assertArrayEquals(T[] expected, T[] actual, BiConsumer<T, T> assertions, Comparator<T> cmp) {
        if (expected == null)
            assertNull(actual);
        else {
            assertNotNull(actual);

            assertEquals(expected.length, actual.length);

            List<T> exSorted = cmp == null
                    ? List.of(expected)
                    : Arrays.stream(expected)
                    .sorted(cmp)
                    .collect(Collectors.toList());

            List<T> acSorted = cmp == null
                    ? List.of(actual)
                    : Arrays.stream(actual)
                    .sorted(cmp)
                    .collect(Collectors.toList());

            IntStream
                    .range(0, exSorted.size())
                    .forEach(i -> assertions.accept(exSorted.get(i), acSorted.get(i)));
        }
    }
}
