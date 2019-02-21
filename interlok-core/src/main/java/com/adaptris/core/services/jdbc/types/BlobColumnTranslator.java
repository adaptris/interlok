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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Blob;
import java.sql.SQLException;
import com.adaptris.jdbc.JdbcResultRow;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Column Translator implementation for handling BLOB types
 *
 * @config jdbc-type-blob-column-translator
 *
 * @author lchan
 *
 */
@XStreamAlias("jdbc-type-blob-column-translator")
public class BlobColumnTranslator extends ColumnWriterWithCharEncoding {

  public BlobColumnTranslator() {

  }

  public BlobColumnTranslator(String charEncoding) {
    this();
    setCharacterEncoding(charEncoding);
  }

  @Override
  public String translate(JdbcResultRow rs, int column) throws SQLException, IOException {
    return toString((Blob) rs.getFieldValue(column));
  }

  @Override
  public String translate(JdbcResultRow rs, String columnName) throws SQLException, IOException {
    return toString((Blob) rs.getFieldValue(columnName));
  }

  @Override
  public void write(JdbcResultRow rs, int column, OutputStream out)
      throws SQLException, IOException {
    write((Blob) rs.getFieldValue(column), out);
  }

  @Override
  public void write(JdbcResultRow rs, String columnName, OutputStream out)
      throws SQLException, IOException {
    write((Blob) rs.getFieldValue(columnName), out);
  }

  private void write(Blob blob, Writer out) throws SQLException, IOException {
    try (Reader input = toReader(blob.getBinaryStream())) {
      copy(input, out);
    }
  }

  private void write(Blob blob, OutputStream out) throws SQLException, IOException {
    try (InputStream input = blob.getBinaryStream()) {
      copy(input, out);
    }
  }

  private String toString(Blob blob) throws SQLException, IOException {
    StringWriter output = new StringWriter();
    write(blob, output);
    return output.toString();
  }


}
