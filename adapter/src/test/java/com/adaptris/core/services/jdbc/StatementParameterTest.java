package com.adaptris.core.services.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.ServiceException;

public class StatementParameterTest {

  private static final String STRING_VALUE = "ABCDEFG";

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testConvertString() throws Exception {
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass(String.class.getCanonicalName());
    assertEquals(STRING_VALUE, sp.convertToQueryClass(STRING_VALUE));
  }

  @Test
  public void testConvertNoClass() throws Exception {
    StatementParameter sp = new StatementParameter();
    try {
      sp.convertToQueryClass(STRING_VALUE);
      fail("Expected ServiceException");
    }
    catch (ServiceException expected) {
      // expected
    }
  }

  @Test
  public void testConvertToNonString() throws Exception {
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass(SimpleStringWrapper.class.getCanonicalName());
    SimpleStringWrapper wrapper = new SimpleStringWrapper(STRING_VALUE);
    assertEquals(wrapper, sp.convertToQueryClass(STRING_VALUE));
  }

  @Test
  public void testConvertNull() throws Exception {
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass(String.class.getCanonicalName());
    sp.setConvertNull(false);
    assertNull(sp.convertToQueryClass(null));
  }

  @Test
  public void testConvertWithConvertNull() throws Exception {
    StatementParameter sp = new StatementParameter();
    sp.setQueryClass(String.class.getCanonicalName());
    sp.setConvertNull(true);
    assertEquals("", sp.convertToQueryClass(null));
  }

}
