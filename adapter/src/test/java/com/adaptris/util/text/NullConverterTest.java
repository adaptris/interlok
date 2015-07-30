package com.adaptris.util.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NullConverterTest {

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testNullToEmptyString() throws Exception {
    NullConverter c = new NullToEmptyStringConverter();
    assertNotNull(c.convert((String) null));
    assertEquals("", c.convert((String) null));
    assertEquals("ABC", c.convert("ABC"));
  }

  @Test
  public void testNullsNotSupported() throws Exception {
    NullConverter c = new NullsNotSupportedConverter();
    String x = null;
    try {
      c.convert(x);
      fail("Converted a null object when you should have");
    }
    catch (UnsupportedOperationException expected) {

    }
    assertEquals("ABC", c.convert("ABC"));
  }

  @Test
  public void testNullsPassThrough() throws Exception {
    NullConverter c = new NullPassThroughConverter();
    String x = null;
    assertNull(c.convert(x));
  }

}
