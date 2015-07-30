package com.adaptris.core.services.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.ServiceException;

public class TimestampParameterTest {

  private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
  private String timestampString;
  private java.sql.Timestamp timestamp;

  @Before
  public void setUp() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT);
    timestampString = sdf.format(new java.util.Date());
    timestamp = new java.sql.Timestamp(sdf.parse(timestampString).getTime());
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testConvert() throws Exception {
    TimestampStatementParameter sp = create();
    assertEquals(timestamp, sp.convertToQueryClass(timestampString));
  }

  @Test
  public void testConvertWithQueryClass() throws Exception {
    TimestampStatementParameter sp = create();
    sp.setQueryClass("java.lang.String");
    assertEquals(timestamp, sp.convertToQueryClass(timestampString));
  }

  @Test
  public void testUnparseableFormat() throws Exception {
    TimestampStatementParameter sp = create();
    sp.setDateFormat("yyyy-MM-ddHH:mm:ss");
    sp.setConvertNull(false);
    try {
      sp.convertToQueryClass(timestampString);
      fail("Expected ServiceException");
    }
    catch (ServiceException expected) {
      // expected
    }
  }

  @Test
  public void testConvertNull() throws Exception {
    TimestampStatementParameter sp = create();
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
    TimestampStatementParameter sp = create();
    sp.setConvertNull(true);
    long convertedTime = ((java.sql.Timestamp) sp.convertToQueryClass(null)).getTime();
    long now = System.currentTimeMillis();
    assertTrue("now > convertedTime", now >= convertedTime);
  }

  private TimestampStatementParameter create() throws Exception {
    TimestampStatementParameter sp = new TimestampStatementParameter();
    sp.setDateFormat(DEFAULT_TIMESTAMP_FORMAT);
    return sp;
  }
}
