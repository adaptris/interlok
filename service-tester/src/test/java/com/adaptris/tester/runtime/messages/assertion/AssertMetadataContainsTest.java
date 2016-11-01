package com.adaptris.tester.runtime.messages.assertion;

import com.adaptris.util.KeyValuePairSet;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AssertMetadataContainsTest {

  @Test
  public void execute() throws Exception {
    Map<String, String> expected = new HashMap<>();
    expected.put("key1", "val1");
    Map<String, String> actual = new HashMap<>();
    actual.put("key1", "val1");
    actual.put("key2", "val2");
    MetadataAssertion matcher = new AssertMetadataContains();
    KeyValuePairSet kvp = new KeyValuePairSet(expected);
    matcher.setMetadata(kvp);
    assertTrue(matcher.execute(actual).isPassed());
    actual.put("key1", "valother");
    assertFalse(matcher.execute(actual).isPassed());
    expected.remove("key1");
    expected.put("key3", "val3");
    assertFalse(matcher.execute(actual).isPassed());
  }

}