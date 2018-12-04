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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Arrays;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Test;
import com.adaptris.jdbc.JdbcResultRow;

@SuppressWarnings("deprecation")
public class BlobColumnTranslatorTest {



  @Test
  public void testBlobToString_WithEncoding() throws Exception {
    BlobColumnTranslator translator = new BlobColumnTranslator("UTF-8");
    TestBlob blob = new TestBlob();
    String myData = new String("SomeData");
    blob.setBytes(0, myData.getBytes("UTF-8"));

    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", blob);

    String translated = translator.translate(row, 0);

    assertEquals("SomeData", translated);
  }

  @Test
  public void testBlobToString() throws Exception {
    BlobColumnTranslator translator = new BlobColumnTranslator();
    TestBlob blob = new TestBlob();
    String myData = new String("SomeData");
    blob.setBytes(0, myData.getBytes());

    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", blob);

    String translated = translator.translate(row, 0);

    assertEquals("SomeData", translated);
  }

  @Test
  public void testBlobToStringWithColumnName() throws Exception {
    BlobColumnTranslator translator = new BlobColumnTranslator();
    TestBlob blob = new TestBlob();
    String myData = new String("SomeData");
    blob.setBytes(0, myData.getBytes());

    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", blob);

    String translated = translator.translate(row, "testField");

    assertEquals("SomeData", translated);
  }

  @Test
  public void testBlobToStringWrongType() throws Exception {
    BlobColumnTranslator translator = new BlobColumnTranslator();
    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", "SomeData");

    try {
      translator.translate(row, "testField");
      fail();
    } catch (Exception ex) {
      // pass, expected
    }
  }

  @Test
  public void testBlobWrite_WithEncoding() throws Exception {
    BlobColumnTranslator translator = new BlobColumnTranslator("UTF-8");
    TestBlob blob = new TestBlob();
    String myData = new String("SomeData");
    blob.setBytes(0, myData.getBytes("UTF-8"));

    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", blob);

    StringWriter writer = new StringWriter();
    try (OutputStream out = new WriterOutputStream(writer)) {
      translator.write(row, 0, out);
    }
    String translated = writer.toString();

    assertEquals("SomeData", translated);
  }

  @Test
  public void testBlobWrite() throws Exception {
    BlobColumnTranslator translator = new BlobColumnTranslator();
    TestBlob blob = new TestBlob();
    String myData = new String("SomeData");
    blob.setBytes(0, myData.getBytes());

    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", blob);
    StringWriter writer = new StringWriter();
    try (OutputStream out = new WriterOutputStream(writer)) {
      translator.write(row, 0, out);
    }
    String translated = writer.toString();
    assertEquals("SomeData", translated);
  }

  @Test
  public void testBlobWrite_ColumnName() throws Exception {
    BlobColumnTranslator translator = new BlobColumnTranslator();
    TestBlob blob = new TestBlob();
    String myData = new String("SomeData");
    blob.setBytes(0, myData.getBytes());

    JdbcResultRow row = new JdbcResultRow();
    row.setFieldValue("testField", blob);
    StringWriter writer = new StringWriter();
    try (OutputStream out = new WriterOutputStream(writer)) {
      translator.write(row, "testField", out);
    }
    String translated = writer.toString();
    assertEquals("SomeData", translated);
  }


  private class TestBlob implements java.sql.Blob {

    private byte[] data = new byte[0];

    @Override
    public void free() throws SQLException {
      data = new byte[0];
    }

    @Override
    public InputStream getBinaryStream() throws SQLException {
      return new ByteArrayInputStream(data);
    }

    @Override
    public InputStream getBinaryStream(long pos, long length) throws SQLException {
      return new ByteArrayInputStream(getBytes(pos, (int) length));
    }

    @Override
    public byte[] getBytes(long pos, int length) throws SQLException {
      return Arrays.copyOfRange(data, (int) pos, length);
    }

    @Override
    public long length() throws SQLException {
      return data.length;
    }

    @Override
    public long position(byte[] pattern, long start) throws SQLException {
      return 0;
    }

    @Override
    public long position(java.sql.Blob pattern, long start) throws SQLException {
      return 0;
    }

    @Override
    public OutputStream setBinaryStream(long pos) throws SQLException {
      return new ByteArrayOutputStream((int) pos);
    }

    @Override
    public int setBytes(long pos, byte[] bytes) throws SQLException {
      int aLen = data.length;
      int bLen = bytes.length;
      byte[] concatted = new byte[aLen+bLen];
      System.arraycopy(data, 0, concatted, 0, aLen);
      System.arraycopy(bytes, 0, concatted, aLen, bLen);

      data = concatted;
      return bytes.length;
    }

    @Override
    public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
      return this.setBytes(pos, Arrays.copyOfRange(bytes, offset, len));
    }

    @Override
    public void truncate(long len) throws SQLException {
      data = getBytes(0, (int) len);
    }

  }
}
