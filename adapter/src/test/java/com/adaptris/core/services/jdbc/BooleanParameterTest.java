package com.adaptris.core.services.jdbc;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BooleanParameterTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testConvert() throws Exception {
    BooleanStatementParameter sp = new BooleanStatementParameter();
    assertEquals(Boolean.TRUE, sp.convertToQueryClass("on"));
  }

  @Test
  public void testConvertWithQueryClass() throws Exception {
    BooleanStatementParameter sp = new BooleanStatementParameter();
    sp.setQueryClass("java.lang.String");
    assertEquals(Boolean.TRUE, sp.convertToQueryClass("on"));
  }

  @Test
  public void testConvertNull() throws Exception {
    BooleanStatementParameter sp = new BooleanStatementParameter();
    assertEquals(Boolean.FALSE, sp.convertToQueryClass(""));
  }

}
