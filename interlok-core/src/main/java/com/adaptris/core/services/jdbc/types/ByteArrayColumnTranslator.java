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

import static org.apache.commons.io.IOUtils.copy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;

import com.adaptris.jdbc.JdbcResultRow;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Column Translator implementation for handling byte array types.
 * <p>
 * Note that this is a largely redundant translator and is only included for completeness; the column types that will be used are
 * generally going to be BLOB or CLOB; I wouldn't really expect to see many types that have to be explicitly treated as a byte array
 * </p>
 *
 * @config jdbc-type-byte-array-column-translator
 *
 * @author lchan
 *
 */
@XStreamAlias("jdbc-type-byte-array-column-translator")
public class ByteArrayColumnTranslator extends ColumnWriterWithCharEncoding {

  public ByteArrayColumnTranslator() {
  }

  @Override
  public String translate(JdbcResultRow rs, int column) throws SQLException, IOException {
    return toString((byte[]) rs.getFieldValue(column));
  }

  @Override
  public String translate(JdbcResultRow rs, String columnName) throws SQLException, IOException {
    return toString((byte[]) rs.getFieldValue(columnName));
  }

  @Override
  public void write(JdbcResultRow rs, int column, OutputStream out)
      throws SQLException, IOException {
    write((byte[]) rs.getFieldValue(column), out);
  }

  @Override
  public void write(JdbcResultRow rs, String columnName, OutputStream out)
      throws SQLException, IOException {
    write((byte[]) rs.getFieldValue(columnName), out);
  }

  private void write(byte[] bytes, Writer out) throws SQLException, IOException {
    try (Reader input = toReader(new ByteArrayInputStream(bytes))) {
      copy(input, out);
    }
  }

  private void write(byte[] bytes, OutputStream out) throws SQLException, IOException {
    IOUtils.write(bytes, out);
  }

  private String toString(byte[] bytes) throws SQLException, IOException {
    StringWriter output = new StringWriter();
    write(bytes, output);
    return output.toString();
  }
}
