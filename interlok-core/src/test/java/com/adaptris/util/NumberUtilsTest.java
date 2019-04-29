package com.adaptris.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NumberUtilsTest extends NumberUtils {

  @Test
  public void testToIntDefaultIfNull() {
    assertEquals(1, toIntDefaultIfNull(Integer.valueOf(1), 10));
    assertEquals(10, toIntDefaultIfNull(null, 10));
  }

  @Test
  public void testToLongDefaultIfNull() {
    assertEquals(1L, toLongDefaultIfNull(Long.valueOf(1), 10L));
    assertEquals(10L, toLongDefaultIfNull(null, 10L));
  }

}
