/*
 * $RCSfile: ByteTranslatorTest.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/07/24 11:05:03 $
 * $Author: lchan $
 */
package com.adaptris.util.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class DateFormatUtilTest {

  private static final String[] DATE_FORMATS =
  {
      "yyyy-MM-dd'T'HH:mm:ssZ", "yyyyMMdd HH:mm:ss zzz", "yyyyMMdd HH:mm:ss",
      "yyyyMMdd", "yyyy-MM-dd HH:mm:ss zzz", "yyyy-MM-dd HH:mm:ss",
      "yyyy-MM-dd", "dd.MM.yyyy HH:mm:ss zzz", "dd.MM.yyyy HH:mm:ss",
      "dd.MM.yyyy", "dd/MM/yyyy HH:mm:ss zzz", "dd/MM/yyyy HH:mm:ss",
      "dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy",
  };


  @Test
  public void testDateParser() {
    Date dateToParse = new Date(System.currentTimeMillis() - 3600 * 48);
    Date now = new Date(System.currentTimeMillis());
    for (int i=0; i < DATE_FORMATS.length; i++) {
      SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMATS[i]);
      String datetime = sdf.format(dateToParse);
      Date parsedDate = DateFormatUtil.parse(datetime);
      assertNotNull(parsedDate);
      assertTrue(now + " should be after " + parsedDate, now.after(parsedDate));
    }
  }

  @Test
  public void testDateParserWithNull() {
    Date parsedDate = DateFormatUtil.parse(null);
    Date date = new Date(System.currentTimeMillis() + 3600);
    assertNotNull(parsedDate);
    assertTrue(date.after(parsedDate));

  }

  @Test
  public void testDateParserWithUnParseable() {
    Date parsedDate = DateFormatUtil.parse("ABCDEFG");
    Date date = new Date(System.currentTimeMillis() + 3600);
    assertNotNull(parsedDate);
    assertTrue(date.after(parsedDate));

  }

  @Test
  public void testDateFormatter() {
    Date date = new Date(System.currentTimeMillis() - 3600);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    String datetime = sdf.format(date);
    assertEquals(datetime, DateFormatUtil.format(date));
  }


}
