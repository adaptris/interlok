package com.adaptris.core.services.jdbc.types;

import java.io.IOException;
import java.sql.SQLException;

import com.adaptris.jdbc.JdbcResultRow;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Column Translator implementation for handling boolean types
 * 
 * @config jdbc-type-boolean-column-translator
 * 
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-type-boolean-column-translator")
public class BooleanColumnTranslator implements ColumnTranslator {

  public BooleanColumnTranslator() {
  }

  @Override
  public String translate(JdbcResultRow rs, int column) throws SQLException, IOException {
    return String.valueOf(rs.getFieldValue(column));
  }

  @Override
  public String translate(JdbcResultRow rs, String column) throws SQLException, IOException {
    return String.valueOf(rs.getFieldValue(column));
  }

}
