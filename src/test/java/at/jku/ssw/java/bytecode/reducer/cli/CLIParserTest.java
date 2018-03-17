package at.jku.ssw.java.bytecode.reducer.cli;


import at.jku.ssw.java.bytecode.reducer.context.ContextFactory;
import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static at.jku.ssw.java.bytecode.reducer.context.ContextFactory.*;
import static org.junit.jupiter.api.Assertions.*;

public class CLIParserTest {
    //-------------------------------------------------------------------------
    // region Test utilities

    private static ContextFactory parse(String... args) throws ParseException {
        return new CLIParser().parseArguments(args);
    }

    private static ContextFactory emptyContextFactory() {
        return new ContextFactory(
                new String[0],
                new String[0],
                "",
                DEFAULT_OUT,
                DEFAULT_TEMP,
                DEFAULT_THREAD_NUM
        );
    }

    // endregion
    //-------------------------------------------------------------------------
    // region

    @Test
    void testNoArguments() throws ParseException {
        assertEquals(emptyContextFactory(), parse());
    }

    @Test
    void testHelpArgument() throws ParseException {
        final String help = "help";

        assertNull(parse(help));
        assertNull(parse(help, ""));
        assertNull(parse(help, "1"));
        assertNull(parse(help, "another_arg"));
    }

    @Test
    void testVersionArgument() throws ParseException {
        final String version = "version";

        assertNull(parse(version));
        assertNull(parse(version, ""));
        assertNull(parse(version, "1"));
        assertNull(parse(version, "another_arg"));
    }

    @Test
    void testLoggingArgument() throws ParseException {
        assertEquals(emptyContextFactory(), parse("-q"));
        assertEquals(emptyContextFactory(), parse("-v"));
        assertEquals(emptyContextFactory(), parse("-v", "-v"));
        assertEquals(emptyContextFactory(), parse("-q", "-q"));

        assertThrows(AlreadySelectedException.class, () -> parse("-v", "-q"));
        assertThrows(AlreadySelectedException.class, () -> parse("-q", "-v"));
    }

    @Test
    void testUnknownArgument() throws ParseException {
        assertEquals(emptyContextFactory(), parse("-notanoption"));
    }

    @Test
    void testOnlyClassFileArgs() throws ParseException {
        final String[] args = {"file1", "file2", "file3", "1", ""};

        final ContextFactory expected = new ContextFactory(
                Arrays.copyOfRange(args, 1, 5),
                Arrays.copyOf(args, 1),
                "",
                DEFAULT_OUT,
                DEFAULT_TEMP,
                DEFAULT_THREAD_NUM
        );

        assertEquals(expected, parse(args));
    }

    // endregion
    //-------------------------------------------------------------------------
}
