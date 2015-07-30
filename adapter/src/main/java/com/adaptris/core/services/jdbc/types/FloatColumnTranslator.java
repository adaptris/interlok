package com.adaptris.core.services.jdbc.types;

import java.io.IOException;
import java.sql.SQLException;

import com.adaptris.jdbc.JdbcResultRow;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Column Translator implementation for handling float types
 * 
 * @config jdbc-type-float-column-translator
 * 
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-type-float-column-translator")
public class FloatColumnTranslator extends FormattableColumnTranslator {

  public FloatColumnTranslator() {
    super();
  }

  public FloatColumnTranslator(String format) {
    super(format);
  }

  @Override
  public String translate(JdbcResultRow rs, int column) throws SQLException, IOException {
    Object doubleObject = rs.getFieldValue(column);
    if(doubleObject instanceof Float)
      return toString((Float) rs.getFieldValue(column));
    else
      return toString(new Float(rs.getFieldValue(column).toString()));
  }

  @Override
  public String translate(JdbcResultRow rs, String column) throws SQLException, IOException {
    Object doubleObject = rs.getFieldValue(column);
    if(doubleObject instanceof Float)
      return toString((Float) rs.getFieldValue(column));
    else
      return toString(new Float(rs.getFieldValue(column).toString()));
  }

}
