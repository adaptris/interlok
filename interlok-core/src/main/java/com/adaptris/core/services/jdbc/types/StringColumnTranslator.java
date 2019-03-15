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

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;

import com.adaptris.jdbc.JdbcResultRow;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Column Translator implementation for handling string types
 *
 * @config jdbc-type-string-column-translator
 *
 *
 * @author lchan
 *
 */
@XStreamAlias("jdbc-type-string-column-translator")
public class StringColumnTranslator extends FormattableColumnTranslator implements ColumnWriter {

  public StringColumnTranslator() {
  }

  @Override
  public String translate(JdbcResultRow rs, int column) throws SQLException, IOException {
    return toString(rs.getFieldValue(column));
  }

  @Override
  public String translate(JdbcResultRow rs, String columnName) throws SQLException, IOException {
    return toString(rs.getFieldValue(columnName));
  }

  @Override
  public void write(JdbcResultRow rs, int column, OutputStream out)
      throws SQLException, IOException {
    IOUtils.write(toString(rs.getFieldValue(column)), out);
  }

  @Override
  public void write(JdbcResultRow rs, String columnName, OutputStream out)
      throws SQLException, IOException {
    IOUtils.write(toString(rs.getFieldValue(columnName)), out);
  }

}
