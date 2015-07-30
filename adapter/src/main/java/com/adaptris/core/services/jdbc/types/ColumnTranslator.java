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
   * @throws SQLException
   */
  String translate(JdbcResultRow rs, int column) throws SQLException, IOException;

  /**
   * Translate the column into a String.
   *
   * @param rs the result set
   * @param columnName the column name
   * @return The String representation of the column
   * @throws SQLException
   */
  String translate(JdbcResultRow rs, String columnName) throws SQLException, IOException;

}
