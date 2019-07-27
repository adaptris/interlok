package com.adaptris.core.services.conditional.operator;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.services.conditional.Operator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class isEmptyTest {

  private Operator operator;

  private AdaptrisMessage message;

  @Before
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
  public void testMatchIgnoreFlagWhitespaceString() {
    ((IsEmpty)operator).setIgnoreWhitespace(true);
    assertTrue(operator.apply(message, "     \t"));
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
}
