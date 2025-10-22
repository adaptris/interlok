package com.adaptris.core;

import com.adaptris.core.event.AdapterCloseEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExceptionMatcherTest {

    @Test
    public void testRegexExceptionMatcher() {
        Exception messageException = new Exception("__message__ exception");
        IllegalArgumentException illegalException = new IllegalArgumentException("__illegal argument__ exception");
        RegexExceptionMatcher matcher = new RegexExceptionMatcher();
        matcher.setRegex(".*");

        Arrays.stream(RegexExceptionMatcher.MatchAgainstField.values()).forEach(field -> {
            matcher.setMatchAgainstField(field);
            if (field.equals(RegexExceptionMatcher.MatchAgainstField.EXCEPTION_CAUSE)) {
                Assertions.assertFalse(matcher.matches(messageException));
                Assertions.assertFalse(matcher.matches(illegalException));
            } else {
                Assertions.assertTrue(matcher.matches(messageException));
                Assertions.assertTrue(matcher.matches(illegalException));
            }
        });

        matcher.setRegex(".*__message__.*");
        List.of(RegexExceptionMatcher.MatchAgainstField.EXCEPTION_CAUSE,
                RegexExceptionMatcher.MatchAgainstField.STACKTRACE).forEach(field -> {
            matcher.setMatchAgainstField(field);
            Assertions.assertFalse(matcher.matches(messageException));
            Assertions.assertFalse(matcher.matches(illegalException));
        });
        List.of(RegexExceptionMatcher.MatchAgainstField.EXCEPTION,
                RegexExceptionMatcher.MatchAgainstField.EXCEPTION_MESSAGE).forEach(field -> {
            matcher.setMatchAgainstField(field);
            Assertions.assertTrue(matcher.matches(messageException));
            Assertions.assertFalse(matcher.matches(illegalException));
        });
    }

    @Test
    public void testInstanceOfExceptionMatcher() throws Exception {
        UnsupportedOperationException unsupportedException = new UnsupportedOperationException("__unsupported__ exception");
        IllegalArgumentException illegalException = new IllegalArgumentException("__illegal argument__ exception");
        InstanceOfExceptionMatcher matcher = new InstanceOfExceptionMatcher();
        matcher.setClazz(Exception.class);
        Assertions.assertTrue(matcher.matches(unsupportedException));
        Assertions.assertTrue(matcher.matches(illegalException));

        matcher.setClazz(RuntimeException.class);
        Assertions.assertTrue(matcher.matches(unsupportedException));
        Assertions.assertTrue(matcher.matches(illegalException));

        matcher.setClazz(UnsupportedOperationException.class);
        Assertions.assertTrue(matcher.matches(unsupportedException));
        Assertions.assertFalse(matcher.matches(illegalException));

        matcher.setClazz(IllegalArgumentException.class);
        Assertions.assertFalse(matcher.matches(unsupportedException));
        Assertions.assertTrue(matcher.matches(illegalException));

    }

    @Test
    public void testCompositeExceptionMatcher() throws Exception {
        UnsupportedOperationException unsupportedException = new UnsupportedOperationException("__unsupported__ exception");
        IllegalArgumentException illegalException = new IllegalArgumentException("__illegal argument__ exception");

        InstanceOfExceptionMatcher matcher = new InstanceOfExceptionMatcher();
        matcher.setClazz(IllegalArgumentException.class);
        assertEquals(IllegalArgumentException.class, matcher.getClazz());

        RegexExceptionMatcher matcher1 = new RegexExceptionMatcher();
        matcher1.setRegex("__produce__ exception");
        matcher1.setMatchAgainstField(RegexExceptionMatcher.MatchAgainstField.EXCEPTION_MESSAGE);

        CompositeExceptionMatcher composite = new CompositeExceptionMatcher();
        var matchers = List.of(matcher1, matcher);
        composite.setMatchers(matchers);
        assertEquals(matchers, composite.getMatchers());

        Assertions.assertFalse(composite.matches(unsupportedException));
        Assertions.assertTrue(composite.matches(illegalException));
        Assertions.assertFalse(composite.matches(new ProduceException("no match")));
        Assertions.assertTrue(composite.matches(new ProduceException("__produce__ exception")));

    }


}
