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
      return toString((Double) rs.getFieldValue(column));
    else
      return toString(new Double(rs.getFieldValue(column).toString()));
  }

  @Override
  public String translate(JdbcResultRow rs, String column) throws SQLException, IOException {
    Object doubleObject = rs.getFieldValue(column);
    if(doubleObject instanceof Double)
      return toString((Double) rs.getFieldValue(column));
    else
      return toString(new Double(rs.getFieldValue(column).toString()));
  }

}
