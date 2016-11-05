package com.adaptris.tester.runtime.messages.assertion;

import com.adaptris.tester.runtime.messages.TestMessage;
import com.adaptris.util.KeyValuePairSet;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class AssertMetadataNotEqualsTest extends AssertionCase {

  public AssertMetadataNotEqualsTest(String name) {
    super(name);
  }

  @Test
  public void testExecute() throws Exception {
    Map<String, String> expected = new HashMap<>();
    expected.put("key1", "val1");
    Map<String, String> actual = new HashMap<>();
    actual.put("key1", "val1");
    MetadataAssertion matcher = new AssertMetadataNotEquals();
    KeyValuePairSet kvp = new KeyValuePairSet(expected);
    matcher.setMetadata(kvp);
    assertFalse(matcher.execute(new TestMessage(actual,"")).isPassed());
    actual.put("key1", "other");
    assertTrue(matcher.execute(new TestMessage(actual,"")).isPassed());
    actual.remove("key1");
    actual.put("key2", "val2");
    assertTrue(matcher.execute(new TestMessage(actual,"")).isPassed());
  }

  @Test
  public void testExpected(){
    assertEquals("Metadata: {key1=val1}", createAssertion().expected());
  }

  @Test
  public void testGetMessage(){
    AssertionResult result  = createAssertion().execute(new TestMessage());
    assertEquals("Assertion Failure: [assert-metadata-not-equals]", result.getMessage());
  }

  @Override
  protected Assertion createAssertion() {
    Map<String, String> expected = new HashMap<>();
    expected.put("key1", "val1");
    return new AssertMetadataNotEquals(expected);
  }
}