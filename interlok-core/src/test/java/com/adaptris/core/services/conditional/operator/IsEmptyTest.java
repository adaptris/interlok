package com.adaptris.core.services.conditional.operator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.services.conditional.Operator;

public class IsEmptyTest {

  private Operator operator;

  private AdaptrisMessage message;

  @BeforeEach
  public void setUp() throws Exception {
    operator = new IsEmpty();
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
    ((IsEmpty)operator).setIgnoreWhitespace(false);
  }

  @Test
  public void testNoMatch() {
    assertFalse(operator.apply(message, "a string value"));
  }

  @Test
  public void testMatchNull() {
    assertTrue(operator.apply(message, null));
  }

  @Test
  public void testMatchEmptyString() {
    assertTrue(operator.apply(message, ""));
  }

  @Test
  public void testMatchWhitespaceString() {
    assertFalse(operator.apply(message, "     \t"));
  }

  @Test
  public void testMatchMixedString() {
    assertFalse(operator.apply(message, " ABC "));
  }

  @Test
  public void testMatchIgnoreFlagWhitespaceString() {
    ((IsEmpty)operator).setIgnoreWhitespace(true);
    assertTrue(operator.apply(message, "     \t"));
  }

  @Test
  public void testMatchIgnoreFlagWhitespaceMixedString() {
    ((IsEmpty)operator).setIgnoreWhitespace(true);
    assertFalse(operator.apply(message, " ABC \t"));
  }

  @Test
  public void testMatchIgnoreFlagEmptyString() {
    ((IsEmpty)operator).setIgnoreWhitespace(true);
    assertTrue(operator.apply(message, ""));
  }

  @Test
  public void testMatchIgnoreFlagNull() {
    ((IsEmpty)operator).setIgnoreWhitespace(true);
    assertTrue(operator.apply(message, null));
  }
  
  @Test
  public void testToString() {
    assertTrue(operator.toString().contains("is empty"));
  }
}
