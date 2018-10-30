package com.adaptris.transform.ff;

import static org.junit.Assert.*;
import org.junit.Test;

public class StreamParserTest {

    private static final String TEST_STRING = "This is a String";
    private static final String TEST_STRING_ENDING_WITH_SPACES = "This is a String ending with spaces    ";
    private static final String TEST_STRING_STARTING_WITH_SPACES = "     This is a String starting with spaces";

    @Test
    public void readFixedLength_beyondEOF_resultsAsExpected_whenContentsNotEndingWithSpaces() throws Exception {
        readFixedLengthAndAssertResult(TEST_STRING, 100);
    }

    @Test
    public void readFixedLength_beyondEOF_resultsAsExpected_whenContentsEndingWithSpaces() throws Exception {
        readFixedLengthAndAssertResult(TEST_STRING_ENDING_WITH_SPACES, 100);
    }

    @Test
    public void readFixedLength_beyondEOF_resultsAsExpected_whenContentsStartingWithSpaces() throws Exception {
        readFixedLengthAndAssertResult(TEST_STRING_STARTING_WITH_SPACES, 100);
    }

    @Test
    public void readFixedLength_reachEOF_readAgain_isEmpty() throws Exception {
        StreamParser sp = new StreamParser(TEST_STRING);
        sp.setParseRule(StreamParser.FIXED_LENGTH, 100);

        readContentsAndAssertEquals(sp, TEST_STRING);
        readContentsAndAssertEquals(sp, "");
    }

    @Test
    public void readFixedLength_rewindElement_readSeparatedString() throws Exception {
        StreamParser sp = new StreamParser("Part one   Part two|Part three | The end ");

        sp.setParseRule(StreamParser.FIXED_LENGTH, 11);
        readContentsAndAssertEquals(sp, "Part one   ");

        sp.rewindElement(sp.getContent());

        sp.setParseRule(StreamParser.SEPARATED_STRING, "|".charAt(0));
        readContentsAndAssertEquals(sp, "Part one   Part two");

        // Try read beyond EOF, then rewind, and read again

        sp.setParseRule(StreamParser.FIXED_LENGTH, 100);
        readContentsAndAssertEquals(sp,"Part three | The end ");

        sp.rewindElement(sp.getContent());

        sp.setParseRule(StreamParser.SEPARATED_STRING, "|".charAt(0));
        readContentsAndAssertEquals(sp, "Part three ");
    }

    private void readFixedLengthAndAssertResult(String input, int length) throws Exception {
        StreamParser sp = new StreamParser(input);
        sp.setParseRule(StreamParser.FIXED_LENGTH, length);
        readContentsAndAssertEquals(sp, input);
    }

    private void readContentsAndAssertEquals(StreamParser sp, String expected) throws Exception {
        sp.readElement();
        assertEquals(expected, sp.getContent());
    }
}
