package com.adaptris.core.services.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ShortParameterTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testConvert() throws Exception {
    ShortStatementParameter sp = new ShortStatementParameter();
    assertEquals(Short.valueOf((short) 55), sp.convertToQueryClass("55"));
  }

  @Test
  public void testConvertWithQueryClass() throws Exception {
    ShortStatementParameter sp = new ShortStatementParameter();
    sp.setQueryClass("java.lang.String");
    assertEquals(Short.valueOf((short) 55), sp.convertToQueryClass("55"));
  }

  @Test
  public void testConvertNull() throws Exception {
    ShortStatementParameter sp = new ShortStatementParameter();
    sp.setConvertNull(false);
    try {
      sp.convertToQueryClass(null);
      fail("Expected Exception");
    }
    catch (RuntimeException expected) {
      // expected
    }
    try {
      sp.convertToQueryClass("");
      fail("Expected Exception");
    }
    catch (RuntimeException expected) {
      // expected
    }
  }

  @Test
  public void testConvertWithConvertNull() throws Exception {
    ShortStatementParameter sp = new ShortStatementParameter();
    sp.setConvertNull(true);
    assertEquals(Short.valueOf((short) 0), sp.convertToQueryClass(""));
  }

}
