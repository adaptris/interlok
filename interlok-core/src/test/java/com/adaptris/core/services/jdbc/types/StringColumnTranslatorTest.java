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
import java.io.OutputStream;
import java.io.StringWriter;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.jdbc.JdbcResultRow;

@SuppressWarnings("deprecation")
public class StringColumnTranslatorTest {

  private StringColumnTranslator translator;

  @Before
  public void setUp() throws Exception {
    translator = new StringColumnTranslator();
  }

  @Test
  public void testIntegerTranslator() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Integer(111));

    String translated = translator.translate(row, 0);
    assertEquals("111", translated);
  }

  @Test
  public void testIntegerTranslatorColumnName() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Integer(111));

    String translated = translator.translate(row, "testField");
    assertEquals("111", translated);
  }

  @Test
  public void testDoubleTranslator() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Double(111));

    String translated = translator.translate(row, 0);
    assertEquals("111.0", translated);
  }

  @Test
  public void testFloatTranslator() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Float(111));

    String translated = translator.translate(row, 0);
    assertEquals("111.0", translated);
  }

  @Test
  public void testStringTranslator() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new String("111"));

    String translated = translator.translate(row, 0);
    assertEquals("111", translated);
  }

  @Test
  public void testDoubleFormattedTranslator() throws Exception {
    translator.setFormat("%f");
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Double(111));

    String translated = translator.translate(row, 0);
    assertEquals("111.000000", translated);
  }

  @Test
  public void testStringWrite() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new String("SomeData"));
    StringWriter writer = new StringWriter();
    try (OutputStream out = new WriterOutputStream(writer)) {
      translator.write(row, 0, out);
    }
    String translated = writer.toString();
    assertEquals("SomeData", translated);
  }

  @Test
  public void testStringWrite_ByName() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new String("SomeData"));

    StringWriter writer = new StringWriter();
    try (OutputStream out = new WriterOutputStream(writer)) {
      translator.write(row, "testField", out);
    }
    String translated = writer.toString();
    assertEquals("SomeData", translated);
  }

}
