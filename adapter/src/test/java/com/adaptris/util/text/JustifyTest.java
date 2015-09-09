package com.adaptris.util.text;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JustifyTest {


  private static final String LEADING_PADDED = "     ABCDE";
  private static final String TRAILING_PADDED = "ABCDE     ";
  private static final String UNPADDED = "ABCDE";

  @Test
  public void testTrailing() {
    assertEquals(TRAILING_PADDED, Justify.left(UNPADDED, 10));
    assertEquals(TRAILING_PADDED, Justify.trailing(UNPADDED, 10, ' '));
    assertEquals("          ", Justify.trailing(null, 10, ' '));
    assertEquals("          ", Justify.trailing("", 10, ' '));
    assertEquals("ABCDEFGHIJ", Justify.left("ABCDEFGHIJKLM", 10));
  }

  @Test
  public void testLeading() {
    assertEquals(LEADING_PADDED, Justify.right(UNPADDED, 10));
    assertEquals(LEADING_PADDED, Justify.leading(UNPADDED, 10, ' '));
    assertEquals("          ", Justify.leading(null, 10, ' '));
    assertEquals("          ", Justify.leading("", 10, ' '));
    assertEquals("ABCDEFGHIJ", Justify.right("ABCDEFGHIJKLM", 10));
  }

}
