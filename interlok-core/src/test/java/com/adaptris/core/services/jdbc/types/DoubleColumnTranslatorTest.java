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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Types;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.jdbc.JdbcResultRow;

public class DoubleColumnTranslatorTest {

  private DoubleColumnTranslator translator;
  String expected = "123.000000";

  @BeforeEach
  public void setUp() throws Exception {
    translator = new DoubleColumnTranslator();
  }

  @Test
  public void testFormattedFloat() throws Exception {
    translator.setFormat("%f");
    Float floatVal = Float.valueOf("123");

    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", floatVal, Types.DOUBLE);

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
    Double floatVal = Double.valueOf("123");

    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", floatVal, Types.DOUBLE);

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
    Integer floatVal = Integer.valueOf("123");

    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", floatVal, Types.DOUBLE);

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
    row.setFieldValue("testField", floatVal, Types.DOUBLE);

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
    row.setFieldValue("testField", floatVal, Types.DOUBLE);

    try {
      translator.translate(row, 0);
      fail();
    } catch (Exception ex) {
      //expected
    }
  }

}
