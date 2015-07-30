package com.adaptris.core.services.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LongParameterTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testConvert() throws Exception {
    LongStatementParameter sp = new LongStatementParameter();
    assertEquals(Long.valueOf(55), sp.convertToQueryClass("55"));
  }

  @Test
  public void testConvertWithQueryClass() throws Exception {
    LongStatementParameter sp = new LongStatementParameter();
    sp.setQueryClass("java.lang.String");
    assertEquals(Long.valueOf(55), sp.convertToQueryClass("55"));
  }

  @Test
  public void testConvertNull() throws Exception {
    LongStatementParameter sp = new LongStatementParameter();
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
    LongStatementParameter sp = new LongStatementParameter();
    sp.setConvertNull(true);
    assertEquals(Long.valueOf(0), sp.convertToQueryClass(""));
  }

}
