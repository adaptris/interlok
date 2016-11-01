package com.adaptris.tester.runtime.messages.assertion;

import com.adaptris.util.KeyValuePairSet;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Warmanm on 11/10/2016.
 */
public class AssertMetadataNotEqualsTest {
  @Test
  public void execute() throws Exception {
    Map<String, String> expected = new HashMap<>();
    expected.put("key1", "val1");
    Map<String, String> actual = new HashMap<>();
    actual.put("key1", "val1");
    MetadataAssertion matcher = new AssertMetadataNotEquals();
    KeyValuePairSet kvp = new KeyValuePairSet(expected);
    matcher.setMetadata(kvp);
    assertFalse(matcher.execute(actual).isPassed());
    actual.put("key1", "other");
    assertTrue(matcher.execute(actual).isPassed());
    actual.remove("key1");
    actual.put("key2", "val2");
    assertTrue(matcher.execute(actual).isPassed());
  }

}