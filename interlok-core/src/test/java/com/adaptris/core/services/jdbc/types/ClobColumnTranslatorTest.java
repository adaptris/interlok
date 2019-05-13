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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.rowset.serial.SerialClob;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.jdbc.JdbcResultRow;

@SuppressWarnings("deprecation")
public class ClobColumnTranslatorTest {

  private ClobColumnTranslator translator;

  @Before
  public void setUp() throws Exception {
    translator = new ClobColumnTranslator();
  }

  @Test
  public void testClobToString() throws Exception {
    Clob clob = new SerialClob("SomeData".toCharArray());
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", clob, Types.CLOB);

    String translated = translator.translate(row, 0);

    assertEquals("SomeData", translated);
  }

  @Test
  public void testClobToStringColumnName() throws Exception {
    Clob clob = new SerialClob("SomeData".toCharArray());
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", clob, Types.CLOB);

    String translated = translator.translate(row, "testField");

    assertEquals("SomeData", translated);
  }

  @Test
  public void testClobIncorrectType() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Integer(999), Types.CLOB);

    try {
      translator.translate(row, 0);
      fail();
    } catch (Exception ex) {
      // pass, expected
    }
  }

  @Test
  public void testClobIncorrectTypeColumnName() throws Exception {
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", new Integer(999), Types.CLOB);

    try {
      translator.translate(row, "testField");
      fail();
    } catch (Exception ex) {
      // pass, expected
    }
  }

  @Test
  public void testClobWrite() throws Exception {
    Clob clob = new TestClob("SomeData");
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", clob, Types.CLOB);

    StringWriter writer = new StringWriter();
    try (OutputStream out = new WriterOutputStream(writer)) {
      translator.write(row, 0, out);
    }
    String translated = writer.toString();
    assertEquals("SomeData", translated);
  }

  @Test
  public void testClobWrite_ByName() throws Exception {
    Clob clob = new TestClob("SomeData");
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", clob, Types.CLOB);

    StringWriter writer = new StringWriter();
    try (OutputStream out = new WriterOutputStream(writer)) {
      translator.write(row, "testField", out);
    }
    String translated = writer.toString();
    assertEquals("SomeData", translated);
  }

  private class TestClob implements java.sql.Clob {

    private String data;

    TestClob(String s) {
      data = s;
    }

    @Override
    public long length() throws SQLException {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getSubString(long pos, int length) throws SQLException {
      throw new UnsupportedOperationException();
    }

    @Override
    public Reader getCharacterStream() throws SQLException {
      return new StringReader(data);
    }

    @Override
    public InputStream getAsciiStream() throws SQLException {
      return new ReaderInputStream(getCharacterStream());
    }

    @Override
    public long position(String searchstr, long start) throws SQLException {
      throw new UnsupportedOperationException();
    }

    @Override
    public long position(Clob searchstr, long start) throws SQLException {
      throw new UnsupportedOperationException();
    }

    @Override
    public int setString(long pos, String str) throws SQLException {
      throw new UnsupportedOperationException();
    }

    @Override
    public int setString(long pos, String str, int offset, int len) throws SQLException {
      throw new UnsupportedOperationException();
    }

    @Override
    public OutputStream setAsciiStream(long pos) throws SQLException {
      throw new UnsupportedOperationException();
    }

    @Override
    public Writer setCharacterStream(long pos) throws SQLException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void truncate(long len) throws SQLException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void free() throws SQLException {
    }

    @Override
    public Reader getCharacterStream(long pos, long length) throws SQLException {
      throw new UnsupportedOperationException();
    }

  }
}
