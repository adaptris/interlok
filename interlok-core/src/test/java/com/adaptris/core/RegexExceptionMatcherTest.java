package com.adaptris.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegexExceptionMatcherTest {

    private RegexExceptionMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new RegexExceptionMatcher();
    }

    @Test
    void matchesReturnsTrueWhenRegexMatches() {
        matcher.setRegex(".*Exception.*");
        matcher.setMatchAgainstField(RegexExceptionMatcher.MatchAgainstField.EXCEPTION);

        assertTrue(matcher.matches("SomeExceptionOccurred"));
    }

    @Test
    void matchesReturnsFalseWhenRegexDoesNotMatch() {
        matcher.setRegex(".*Error.*");
        matcher.setMatchAgainstField(RegexExceptionMatcher.MatchAgainstField.EXCEPTION);

        assertFalse(matcher.matches("SomeExceptionOccurred"));
    }

    @Test
    void matchesReturnsFalseWhenInputIsNull() {
        matcher.setRegex(".*Exception.*");
        matcher.setMatchAgainstField(RegexExceptionMatcher.MatchAgainstField.EXCEPTION);

        assertFalse(matcher.matches(null));
    }

    @Test
    void matchesRecompilesRegexWhenChanged() {
        matcher.setRegex(".*Exception.*");
        matcher.matches("SomeExceptionOccurred");

        // Change regex and call matches again
        matcher.setRegex(".*Error.*");

        assertFalse(matcher.matches("SomeExceptionOccurred"));
        assertTrue(matcher.matches("SomeErrorOccurred"));
    }

    @Test
    void getValuesReturnsAllMatchAgainstFieldValues() {
        String[] expectedValues = {"EXCEPTION", "EXCEPTION_MESSAGE", "EXCEPTION_CAUSE", "STACKTRACE"};
        assertArrayEquals(expectedValues, RegexExceptionMatcher.MatchAgainstField.getValues());
    }
}