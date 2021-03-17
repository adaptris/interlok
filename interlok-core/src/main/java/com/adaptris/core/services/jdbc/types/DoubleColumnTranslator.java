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
import java.sql.SQLException;
import com.adaptris.jdbc.JdbcResultRow;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Column Translator implementation for handling double types
 *
 * @config jdbc-type-double-column-translator
 *
 * @author lchan
 *
 */
@XStreamAlias("jdbc-type-double-column-translator")
public class DoubleColumnTranslator extends FormattableColumnTranslator {

  public DoubleColumnTranslator() {
    super();
  }

  public DoubleColumnTranslator(String format) {
    super(format);
  }

  @Override
  public String translate(JdbcResultRow rs, int column) throws SQLException, IOException {
    Object doubleObject = rs.getFieldValue(column);
    if(doubleObject instanceof Double)
      return toString(rs.getFieldValue(column));
    else
      return toString(Double.valueOf(rs.getFieldValue(column).toString()));
  }

  @Override
  public String translate(JdbcResultRow rs, String column) throws SQLException, IOException {
    Object doubleObject = rs.getFieldValue(column);
    if(doubleObject instanceof Double)
      return toString(rs.getFieldValue(column));
    else
      return toString(Double.valueOf(rs.getFieldValue(column).toString()));
  }

}
