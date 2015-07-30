package com.adaptris.core.services.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FloatParameterTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testConvert() throws Exception {
    FloatStatementParameter sp = new FloatStatementParameter();
    assertEquals(Float.valueOf(55.0f), sp.convertToQueryClass("55.0"));
  }

  @Test
  public void testConvertWithQueryClass() throws Exception {
    FloatStatementParameter sp = new FloatStatementParameter();
    sp.setQueryClass("java.lang.String");
    assertEquals(Float.valueOf(55.0f), sp.convertToQueryClass("55.0"));
  }

  @Test
  public void testConvertNull() throws Exception {
    FloatStatementParameter sp = new FloatStatementParameter();
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
    FloatStatementParameter sp = new FloatStatementParameter();
    sp.setConvertNull(true);
    assertEquals(Float.valueOf(0), sp.convertToQueryClass(""));
  }

}
