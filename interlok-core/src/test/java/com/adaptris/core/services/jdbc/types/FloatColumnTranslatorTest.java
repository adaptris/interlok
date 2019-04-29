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

import org.junit.Before;
import org.junit.Test;

import com.adaptris.jdbc.JdbcResultRow;

public class FloatColumnTranslatorTest {

  private FloatColumnTranslator translator;
  String expected = "123.000000";

  @Before
  public void setUp() throws Exception {
    translator = new FloatColumnTranslator();
  }

  @Test
  public void testFormattedFloat() throws Exception {
    translator.setFormat("%f");
    Float floatVal = new Float("123");
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", floatVal, Types.FLOAT);
    {
      String translated = translator.translate(row, 0);
      assertEquals(expected, translated);
    }
    {
      String translated = translator.translate(row, "testField");
      assertEquals(expected, translated);
    }
  }

  @Test
  public void testFormattedDouble() throws Exception {
    translator.setFormat("%f");
    Double floatVal = new Double("123");
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", floatVal, Types.FLOAT);
    {
      String translated = translator.translate(row, 0);
      assertEquals(expected, translated);
    }
    {
      String translated = translator.translate(row, "testField");
      assertEquals(expected, translated);
    }
  }

  @Test
  public void testFormattedInteger() throws Exception {
    translator.setFormat("%f");
    Integer floatVal = new Integer("123");
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", floatVal, Types.FLOAT);
    {
      String translated = translator.translate(row, 0);
      assertEquals(expected, translated);
    }
    {
      String translated = translator.translate(row, "testField");
      assertEquals(expected, translated);
    }
  }

  @Test
  public void testFormattedString() throws Exception {
    translator.setFormat("%f");
    String floatVal = new String("123");
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", floatVal, Types.FLOAT);
    {
      String translated = translator.translate(row, 0);
      assertEquals(expected, translated);
    }
    {
      String translated = translator.translate(row, "testField");
      assertEquals(expected, translated);
    }
  }

  @Test
  public void testIllegalFormat() throws Exception {
    translator.setFormat("%zZX");
    String floatVal = new String("123");

    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", floatVal, Types.FLOAT);
    try {
      translator.translate(row, 0);
      fail();
    } catch (Exception ex) {
      //expected
    }
    try {
      translator.translate(row, "testField");
      fail();
    } catch (Exception ex) {
      // expected
    }
  }

}
