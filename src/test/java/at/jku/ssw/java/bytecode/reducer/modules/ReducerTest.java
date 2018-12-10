package at.jku.ssw.java.bytecode.reducer.modules;

import at.jku.ssw.java.bytecode.reducer.Properties;
import at.jku.ssw.java.bytecode.reducer.runtypes.Reducer;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Assertions;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import samples.BytecodeSample.Bytecode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

abstract class ReducerTest {

    protected static class ResultAssertion {
        private final Bytecode bytecode;


        private ResultAssertion(Bytecode bytecode) {
            this.bytecode = bytecode;
        }

        public ReducesTo to(byte[] expected) {
            return new ReducesTo(bytecode, expected);
        }
    }

    protected static class ReducesTo extends TypeSafeMatcher<Class<? extends Reducer>> {

        private final Bytecode bytecode;
        private final byte[] expected;
        private final GeneratedClassVerifier verifier;

        private ReducesTo(Bytecode bytecode, byte[] expected) {
            this.bytecode = bytecode;
            this.expected = expected;
            this.verifier = new GeneratedClassVerifier();
        }

        public static ResultAssertion reduces(Bytecode bytecode) {
            return new ResultAssertion(bytecode);
        }

        @Override
        protected boolean matchesSafely(Class<? extends Reducer> type) {

            Reducer reducer;
            try {
                reducer = type.getConstructor().newInstance();
            } catch (Exception e) {
                throw new AssertionError("Could not instantiate reducer", e);
            }

            byte[] actual;
            try {
                actual = reducer.apply(bytecode.bytecode);

                if (Properties.DEBUG) {
                    var expectedPath = Paths.get(Properties.DIR).resolve("expected");
                    var actualPath = Paths.get(Properties.DIR).resolve("actual");

                    try {
                        if (!Files.exists(expectedPath)) {
                            Files.createDirectories(expectedPath);
                        }

                        if (!Files.exists(actualPath)) {
                            Files.createDirectories(actualPath);
                        }

                        Files.write(
                                expectedPath.resolve(bytecode.className + ".class"),
                                expected
                        );

                        Files.write(
                                actualPath.resolve(bytecode.className + ".class"),
                                actual
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                assertEqualClassStructure(expected, actual);


                return verifier.isValid(bytecode);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("reduces to valid class file");
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
                    assertEquals(fEx.name, fAc.name, fEx.name);
                    assertEquals(fEx.access, fAc.access, fEx.name);
                    assertEquals(fEx.signature, fAc.signature, fEx.name);
                    assertEquals(fEx.value, fAc.value, fEx.name);
                    // TODO maybe compare annotations
                },
                Comparator.comparing(f -> f.name)
        );

        assertListEquals(
                nEx.methods,
                nAc.methods,
                (mEx, mAc) -> {
                    assertEquals(mEx.name, mAc.name, mEx.name);
                    assertEquals(mEx.access, mAc.access, mEx.name);
                    assertEquals(mEx.signature, mAc.signature, mEx.name);
                    assertEquals(mEx.maxLocals, mAc.maxLocals, mEx.name);
                    assertEquals(mEx.maxStack, mAc.maxStack, mEx.name);

                    assertListEquals(
                            mEx.parameters,
                            mAc.parameters,
                            (pEx, pAc) -> {
                                assertEquals(pEx.access, pAc.access, pEx.name);
                                assertEquals(pEx.name, pAc.name, pEx.name);
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
                                assertEquals(iEx.getOpcode(), iAc.getOpcode(), iEx.getOpcode());
                                assertEquals(iEx.getType(), iAc.getType(), iEx.getOpcode());
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
