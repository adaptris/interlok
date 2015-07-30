package com.adaptris.core.services.jdbc.types;

import java.io.IOException;
import java.sql.SQLException;

import com.adaptris.jdbc.JdbcResultRow;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Column Translator implementation for handling integer types
 * 
 * @config jdbc-type-integer-column-translator
 * 
 * 
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-type-integer-column-translator")
public class IntegerColumnTranslator extends FormattableColumnTranslator {

  public IntegerColumnTranslator() {
  }

  @Override
  public String translate(JdbcResultRow rs, int column) throws SQLException, IOException {
    Object integerObject = rs.getFieldValue(column);
    if(integerObject instanceof Integer)
      return toString((Integer) rs.getFieldValue(column));
    else
      return toString(new Integer(rs.getFieldValue(column).toString()));
  }

  @Override
  public String translate(JdbcResultRow rs, String column) throws SQLException, IOException {
    Object integerObject = rs.getFieldValue(column);
    if(integerObject instanceof Integer)
      return toString((Integer) rs.getFieldValue(column));
    else
      return toString(new Integer(rs.getFieldValue(column).toString()));
  }

}
