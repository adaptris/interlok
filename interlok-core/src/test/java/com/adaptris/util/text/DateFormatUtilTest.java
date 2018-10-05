/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.util.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
  public void testDateParser_toDate() throws Exception {
    Date date = new Date(System.currentTimeMillis() - 3600);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    String datetime = sdf.format(date);
    assertNotNull(DateFormatUtil.toDate(datetime, "yyyy-MM-dd'T'HH:mm:ssZ"));
  }

  @Test
  public void testDateParser_toDate_MS_Epoch() throws Exception {
    Date date = new Date(System.currentTimeMillis() - 3600);
    String datetime = String.valueOf(date.getTime());
    assertNotNull(DateFormatUtil.toDate(datetime, DateFormatUtil.CustomDateFormat.MILLISECONDS_SINCE_EPOCH.name()));
  }

  @Test
  public void testDateParser_toDate_Secs_Epoc() throws Exception {
    Date date = new Date(System.currentTimeMillis() - 3600);
    String datetime = String.valueOf(new BigDecimal(date.getTime()).divide(new BigDecimal(1000), RoundingMode.HALF_UP).longValue());
    assertNotNull(DateFormatUtil.toDate(datetime, DateFormatUtil.CustomDateFormat.SECONDS_SINCE_EPOCH.name()));
  }

  @Test
  public void testDateFormatter() {
    Date date = new Date(System.currentTimeMillis() - 3600);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    String datetime = sdf.format(date);
    assertEquals(datetime, DateFormatUtil.format(date));
  }

  @Test
  public void testDateFormatter_toString() {
    Date date = new Date(System.currentTimeMillis() - 3600);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    String datetime = sdf.format(date);
    assertEquals(datetime, DateFormatUtil.toString(date, "yyyy-MM-dd'T'HH:mm:ssZ"));
  }

  @Test
  public void testDateFormatter_toString_MS_epoch() {
    Date date = new Date(System.currentTimeMillis() - 3600);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    String datetime = String.valueOf(date.getTime());
    assertEquals(datetime, DateFormatUtil.toString(date, DateFormatUtil.CustomDateFormat.MILLISECONDS_SINCE_EPOCH.name()));
  }

  @Test
  public void testDateFormatter_toString_Secs_epoch() {
    Date date = new Date(System.currentTimeMillis() - 3600);
    String datetime = String.valueOf(new BigDecimal(date.getTime()).divide(new BigDecimal(1000), RoundingMode.HALF_UP).longValue());
    assertEquals(datetime, DateFormatUtil.toString(date, DateFormatUtil.CustomDateFormat.SECONDS_SINCE_EPOCH.name()));
  }
}
