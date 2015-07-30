package com.adaptris.core.services.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class IntegerValueMatcherTest {

  private static final String GREATER_THAN1 = "GreaterThan";
  private static final String GREATER_THAN_EQUAL = "GreaterThanEqual";
  private static final String LESS_THAN_EQUAL = "LessThanEqual";
  private static final String LESS_THAN = "LessThan";
  private static final String EQUALS = "Equals";

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testEquals() throws Exception {
    IntegerValueMatcher matcher = new IntegerValueMatcher();
    assertEquals(EQUALS, matcher.getNextServiceId("10", create()));
  }

  @Test
  public void testLessThan() throws Exception {
    IntegerValueMatcher matcher = new IntegerValueMatcher();
    assertEquals(LESS_THAN, matcher.getNextServiceId("4", create()));
  }

  @Test
  public void testLessThanEqualTo() throws Exception {
    IntegerValueMatcher matcher = new IntegerValueMatcher();
    assertEquals(LESS_THAN_EQUAL, matcher.getNextServiceId("9", create()));
  }

  @Test
  public void testGreaterThanEqualTo() throws Exception {
    IntegerValueMatcher matcher = new IntegerValueMatcher();
    assertEquals(GREATER_THAN_EQUAL, matcher.getNextServiceId("15", create()));
  }

  @Test
  public void testGreaterThan() throws Exception {
    IntegerValueMatcher matcher = new IntegerValueMatcher();
    assertEquals(GREATER_THAN1, matcher.getNextServiceId("111", create()));
  }

  @Test
  public void testNoMatch() throws Exception {
    IntegerValueMatcher matcher = new IntegerValueMatcher();
    assertNull(matcher.getNextServiceId("11", create()));
  }

  @Test
  public void testBadServiceKey() throws Exception {
    IntegerValueMatcher matcher = new IntegerValueMatcher();
    assertNull(matcher.getNextServiceId("ABCDEFG", new KeyValuePairSet()));
  }

  @Test
  public void testBadMappings() throws Exception {
    IntegerValueMatcher matcher = new IntegerValueMatcher();
    assertNull(matcher.getNextServiceId("10", createBadMappings()));
  }

  private static KeyValuePairSet createBadMappings() {
    KeyValuePairSet result = new KeyValuePairSet();
    result.addKeyValuePair(new KeyValuePair("123456", EQUALS));
    result.addKeyValuePair(new KeyValuePair("ABCDEFG", EQUALS));
    result.addKeyValuePair(new KeyValuePair(">>10", EQUALS));
    result.addKeyValuePair(new KeyValuePair(">=ABCDEFG", EQUALS));
    return result;
  }

  private static KeyValuePairSet create() {
    KeyValuePairSet result = new KeyValuePairSet();
    result.addKeyValuePair(new KeyValuePair("=10", EQUALS));

    // Natural sort order let's me get away with this!
    result.addKeyValuePair(new KeyValuePair("<5", LESS_THAN));
    result.addKeyValuePair(new KeyValuePair("<=9", LESS_THAN_EQUAL));

    // Natural sort order let's me get away with this!
    result.addKeyValuePair(new KeyValuePair(">100", GREATER_THAN1));
    result.addKeyValuePair(new KeyValuePair(">=15", GREATER_THAN_EQUAL));
    return result;
  }
}
