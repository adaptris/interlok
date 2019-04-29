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

import java.io.OutputStream;
import java.io.StringWriter;
import java.sql.Types;

import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.jdbc.JdbcResultRow;

@SuppressWarnings("deprecation")
public class ByteArrayColumnTranslatorTest {

  private ByteArrayColumnTranslator translator;

  @Before
  public void setUp() throws Exception {
    translator = new ByteArrayColumnTranslator();
  }

  @Test
  public void testByteToString() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "SomeData".getBytes(), Types.ARRAY);

    String translated = translator.translate(row, 0);

    assertEquals("SomeData", translated);
  }

  @Test
  public void testByteToStringEncoded() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "SomeData".getBytes("UTF-8"), Types.ARRAY);

    String translated = translator.translate(row, 0);

    assertEquals("SomeData", translated);
  }

  @Test
  public void testByteToStringColName() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "SomeData".getBytes(), Types.ARRAY);

    String translated = translator.translate(row, "testField");

    assertEquals("SomeData", translated);
  }

  @Test
  public void testByteIncorrectObject() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Integer(10), Types.ARRAY);

    try {
      translator.translate(row, "testField");
      fail();
    } catch(Exception ex) {
      //pass, expected
    }
  }

  @Test
  public void testBytesWrite() throws Exception {

    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "SomeData".getBytes(), Types.ARRAY);

    StringWriter writer = new StringWriter();
    try (OutputStream out = new WriterOutputStream(writer)) {

      translator.write(row, 0, out);
    }
    String translated = writer.toString();
    assertEquals("SomeData", translated);
  }

  @Test
  public void testBytesWrite_ByName() throws Exception {

    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "SomeData".getBytes(), Types.ARRAY);

    StringWriter writer = new StringWriter();
    try (OutputStream out = new WriterOutputStream(writer)) {
      translator.write(row, "testField", out);
    }
    String translated = writer.toString();
    assertEquals("SomeData", translated);
  }
}
