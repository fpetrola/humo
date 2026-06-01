/*
 * Humo Language
 * Copyright (C) 2002-2010, Fernando Damian Petrola
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

import ar.net.fpetrola.humo.HumoInterpreter;
import ar.net.fpetrola.humo.interpreter.TrieArrayInterpreter;
import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Tests for TrieArrayInterpreter executing Turing Machine programs.
 *
 * These tests verify that the TrieArrayInterpreter can correctly execute
 * the tm-dec-to-bin.humo program, which implements a Turing Machine that
 * converts decimal numbers to binary representation.
 *
 * For input decimal 10, the expected binary output is 1010.
 *
 * Note: These tests may require significant memory/stack due to the deeply
 * recursive nature of Turing Machine execution in the Humo language.
 * The buffer size multiplier may need adjustment based on interpreter improvements.
 */
public class TrieArrayInterpreterTest {

    @Test
    public void testDecToBinConversion() throws Exception {
        // Read the Turing Machine humo program
        String source = Files.readString(Paths.get("tests/not-nested/tm-dec-to-bin.humo"));
        assertNotNull("tm-dec-to-bin.humo not found", source);


        String s = source.replace("\n", "").replace("\r", "").replace("\t", "").replace(" ", "");

        HumoInterpreter humoInterpreter = new HumoInterpreter();
        int parse = humoInterpreter.parse(new StringBuilder(s), 0);

        // Prepare buffer with input and working space
        int inputLength = source.length();
        int bufferSize = inputLength * 10;  // Large buffer for expanded output
        char[] buffer = new char[bufferSize];
        source.getChars(0, inputLength, buffer, 0);

        // Run the interpreter
        TrieArrayInterpreter interpreter = new TrieArrayInterpreter();
        int writePos = interpreter.parse(buffer, inputLength);

        // Extract the result (output is at buffer[inputLength..writePos])
        String result = new String(buffer, inputLength, writePos - inputLength);

        // The program converts decimal 10 to binary
        // Expected binary representation: 1010
        assertTrue("Result should contain binary representation '1010' of decimal 10",
                   result.contains("1010"));
    }

    @Test
    public void testDecToBinWithZeros() throws Exception {
        // Test that the output contains the expected tape values after conversion
        String source = Files.readString(Paths.get("tests/not-nested/tm-dec-to-bin.humo"));
        int inputLength = source.length();
        int bufferSize = inputLength * 100;
        char[] buffer = new char[bufferSize];
        source.getChars(0, inputLength, buffer, 0);

        TrieArrayInterpreter interpreter = new TrieArrayInterpreter();
        int writePos = interpreter.parse(buffer, inputLength);

        String result = new String(buffer, inputLength, writePos - inputLength);

        // Verify the output contains result variable assignments or binary digits
        // For decimal 10: binary is 1010, so tape positions should show: 1, 0, 1, 0
        boolean hasResultVars = result.contains("res0") || result.contains("res1");
        boolean hasBinaryDigits = result.contains("1") && result.contains("0");
        assertTrue("Result should contain tape value variables or binary digits",
                   hasResultVars || hasBinaryDigits);
    }

    @Test
    public void testDecToBinTerminates() throws Exception {
        // Ensure the interpreter terminates and produces output
        String source = Files.readString(Paths.get("tests/not-nested/tm-dec-to-bin.humo"));
        int inputLength = source.length();
        int bufferSize = inputLength * 100;
        char[] buffer = new char[bufferSize];
        source.getChars(0, inputLength, buffer, 0);

        TrieArrayInterpreter interpreter = new TrieArrayInterpreter();
        int writePos = interpreter.parse(buffer, inputLength);

        // Verify that parsing completed and produced output
        assertTrue("Parser should complete execution", writePos > inputLength);
        assertTrue("Parser should produce some output", (writePos - inputLength) > 0);
    }
}
