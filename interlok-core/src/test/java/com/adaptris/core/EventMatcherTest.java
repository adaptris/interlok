package com.adaptris.core;

import com.adaptris.core.event.AdapterCloseEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class EventMatcherTest {

    @Test
    public void testNullEventMatcher() throws Exception {
        Event e = EventFactory.create(AdapterCloseEvent.class);
        NullEventMatcher matcher = new NullEventMatcher();
        Assertions.assertFalse(matcher.matches(null));
        Assertions.assertFalse(matcher.matches(e));
    }

    @Test
    public void testRegexEventMatcher() throws Exception {
        Event e1 = EventFactory.create(AdapterCloseEvent.class);
        RegexEventMatcher matcher = new RegexEventMatcher();
        matcher.setMatchTypes(Set.of(EventMatcher.EventMatchType.TYPE.name()));
        matcher.setRegex(".*");
        Assertions.assertTrue(matcher.matches(e1));
        Event e2 = EventFactory.create(MessageLifecycleEvent.class);
        Assertions.assertTrue(matcher.matches(e2));
        matcher.setRegex(AdapterCloseEvent.class.getCanonicalName());
        Assertions.assertTrue(matcher.matches(e1));
        Assertions.assertFalse(matcher.matches(e2));
        matcher.setRegex(MessageLifecycleEvent.class.getCanonicalName());
        Assertions.assertFalse(matcher.matches(e1));
        Assertions.assertTrue(matcher.matches(e2));
    }
}
