package com.adaptris.core.services.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.ServiceException;

public class TimeParameterTest {

  private static final String TIME_FORMAT = "HH:mm:ssZ";
  private String timeString;
  private java.sql.Time time;

  @Before
  public void setUp() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
    timeString = sdf.format(new java.util.Date());
    time = new java.sql.Time(sdf.parse(timeString).getTime());
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testConvert() throws Exception {
    assertEquals(time, create().convertToQueryClass(timeString));
  }

  @Test
  public void testConvertWithQueryClass() throws Exception {
    TimeStatementParameter sp = create();
    sp.setQueryClass("java.lang.String");
    assertEquals(time, sp.convertToQueryClass(timeString));
  }

  @Test
  public void testUnparseableFormat() throws Exception {
    TimeStatementParameter sp = create();
    sp.setDateFormat("yyyy-MM-ddHH:mm:ss");
    try {
      sp.convertToQueryClass(timeString);
      fail("Expected ServiceException");
    }
    catch (ServiceException expected) {
      // expected
    }
  }

  @Test
  public void testConvertNull() throws Exception {
    TimeStatementParameter sp = create();
    sp.setConvertNull(false);
    try {
      sp.convertToQueryClass(null);
      fail("Expected ServiceException");
    }
    catch (ServiceException expected) {
      // expected
    }
  }

  @Test
  public void testConvertWithConvertNull() throws Exception {
    TimeStatementParameter sp = create();
    sp.setConvertNull(true);
    long convertedTime = ((java.sql.Time) sp.convertToQueryClass(null)).getTime();
    long now = System.currentTimeMillis();
    assertTrue(now >= convertedTime);
  }

  private TimeStatementParameter create() throws Exception {
    TimeStatementParameter sp = new TimeStatementParameter();
    sp.setDateFormat(TIME_FORMAT);
    return sp;
  }
}
