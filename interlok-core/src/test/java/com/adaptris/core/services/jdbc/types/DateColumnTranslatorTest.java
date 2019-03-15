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

package com.adaptris.core.services.jdbc.types;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

import com.adaptris.jdbc.JdbcResultRow;

public class DateColumnTranslatorTest {

  private DateColumnTranslator translator;
  private GregorianCalendar gDate;
  private Date date;

  @Before
  public void setUp() throws Exception {
    translator = new DateColumnTranslator();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    date = sdf.parse("2013-08-22");

    gDate = new GregorianCalendar();
    gDate.setTime(date);
  }

  @Test
  public void testDateNoFormat() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", date, Types.DATE);
    {
      String translated = translator.translate(row, 0);
      assertEquals("2013-08-22", translated);
    }
    {
      String translated = translator.translate(row, "testField");
      assertEquals("2013-08-22", translated);
    }
  }

  @Test
  public void testDateFormatted() throws Exception {
    translator.setDateFormat("yyyy");
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", date, Types.DATE);

    {
      String translated = translator.translate(row, 0);
      assertEquals("2013", translated);
    }
    {
      String translated = translator.translate(row, "testField");
      assertEquals("2013", translated);
    }
  }

  @Test
  public void testGregorianNoFormat() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", gDate, Types.DATE);

    {
      String translated = translator.translate(row, 0);
      assertEquals("2013-08-22", translated);
    }
    {
      String translated = translator.translate(row, "testField");
      assertEquals("2013-08-22", translated);
    }
  }

  @Test
  public void testGregorianFormatted() throws Exception {
    translator.setDateFormat("yyyy");
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", gDate, Types.DATE);

    {
      String translated = translator.translate(row, 0);
      assertEquals("2013", translated);
    }
    {
      String translated = translator.translate(row, "testField");
      assertEquals("2013", translated);
    }
  }

  @Test
  public void testString() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "2013-08-22", Types.DATE);

    try {
      translator.translate(row, 0);
      fail();
    } catch (Exception ex) {
      // pass, expected
    }
    try {
      translator.translate(row, "testField");
      fail();
    } catch (Exception ex) {
      // pass, expected
    }
  }

}
