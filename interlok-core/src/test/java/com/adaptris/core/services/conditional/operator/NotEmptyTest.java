package com.adaptris.core.services.conditional.operator;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NotEmptyTest {

  private NotEmpty operator;

  private AdaptrisMessage message;

  @Before
  public void setUp() throws Exception {
    operator = new NotEmpty();
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
    operator.setIgnoreWhitespace(false);
  }

  @Test
  public void testStringNotEmpty() {
    assertTrue(operator.apply(message, "string"));
  }
  @Test
  public void testStringEmpty() {
    assertFalse(operator.apply(message, ""));;
  }

  @Test
  public void testStringEmptyWithSpace() {
    assertTrue(operator.apply(message, " "));
  }

  @Test
  public void testStringNull() {
    assertFalse(operator.apply(message, null));
  }

  @Test
  public void testStringNotEmptyWithSpace() {
    assertTrue(operator.apply(message, "  string"));
  }

  @Test
  public void testMixedStringNotEmpty() {
    assertTrue(operator.apply(message, " string "));
  }

  @Test
  public void testIgnorewhitespaceWithEmptyString() {
    operator.setIgnoreWhitespace(true);
    assertFalse(operator.apply(message, " "));
  }

  @Test
  public void testIgnorewhitespaceWithLeadingSpace() {
    operator.setIgnoreWhitespace(true);
    assertTrue(operator.apply(message, "  string"));
  }

  @Test
  public void testIgnorewhitespaceWithString() {
    operator.setIgnoreWhitespace(true);
    assertTrue(operator.apply(message, "string"));
  }

  @Test
  public void testIgnorewhitespaceWithMixedString() {
    operator.setIgnoreWhitespace(true);
    assertTrue(operator.apply(message, " string "));
  }

  @Test
  public void testMatchWhitespaceString() {
    assertTrue(operator.apply(message, "     \t"));
  }

  @Test
  public void testMatchMixedString() {
    assertTrue(operator.apply(message, " string "));
  }

  @Test
  public void testMatchIgnoreFlagWhitespaceString() {
    operator.setIgnoreWhitespace(true);
    assertFalse(operator.apply(message, "     \t"));
  }

  @Test
  public void testMatchIgnoreFlagWhitespaceMixedString() {
    operator.setIgnoreWhitespace(true);
    assertTrue(operator.apply(message, " string \t"));
  }

}
