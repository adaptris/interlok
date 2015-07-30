package com.adaptris.core.services.jdbc.types;

import java.io.IOException;
import java.sql.SQLException;

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
public class StringColumnTranslator extends FormattableColumnTranslator {

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

}
