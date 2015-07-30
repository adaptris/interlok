package com.adaptris.core.services.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DoubleParameterTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testConvert() throws Exception {
    DoubleStatementParameter sp = new DoubleStatementParameter();
    assertEquals(Double.valueOf(55.0), sp.convertToQueryClass("55.0"));
  }

  @Test
  public void testConvertWithQueryClass() throws Exception {
    DoubleStatementParameter sp = new DoubleStatementParameter();
    sp.setQueryClass("java.lang.String");
    assertEquals(Double.valueOf(55.0), sp.convertToQueryClass("55.0"));
  }

  @Test
  public void testConvertNull() throws Exception {
    DoubleStatementParameter sp = new DoubleStatementParameter();
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
    DoubleStatementParameter sp = new DoubleStatementParameter();
    sp.setConvertNull(true);
    assertEquals(Double.valueOf(0), sp.convertToQueryClass(""));
  }

}
