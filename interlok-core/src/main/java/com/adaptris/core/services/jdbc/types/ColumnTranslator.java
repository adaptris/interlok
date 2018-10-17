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

import com.adaptris.core.services.jdbc.AllRowsMetadataTranslator;
import com.adaptris.core.services.jdbc.FirstRowMetadataTranslator;
import com.adaptris.core.services.jdbc.MergeResultSetIntoXmlPayload;
import com.adaptris.core.services.jdbc.ResultSetTranslator;
import com.adaptris.core.services.jdbc.XmlPayloadTranslator;
import com.adaptris.jdbc.JdbcResultRow;

/**
 * Translate a column in a ResultSet into a String for processing.
 *
 * @see ResultSetTranslator
 * @see XmlPayloadTranslator
 * @see MergeResultSetIntoXmlPayload
 * @see FirstRowMetadataTranslator
 * @see AllRowsMetadataTranslator
 *
 */
public interface ColumnTranslator {

  /**
   * Translate the column into a String.
   *
   * @param rs the result set
   * @param column the column index
   * @return The String representation of the column
   */
  String translate(JdbcResultRow rs, int column) throws SQLException, IOException;

  /**
   * Translate the column into a String.
   *
   * @param rs the result set
   * @param columnName the column name
   * @return The String representation of the column
   */
  String translate(JdbcResultRow rs, String columnName) throws SQLException, IOException;
}