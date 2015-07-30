package com.adaptris.core.services.jdbc.types;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;

import com.adaptris.jdbc.JdbcResultRow;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Column Translator implementation for handling timestamp types
 * 
 * @config jdbc-type-timestamp-column-translator
 * 
 * 
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-type-timestamp-column-translator")
public class TimestampColumnTranslator extends DateColumnTranslator {

  /**
   * The default dateformat is "yyyy-MM-dd'T'HH:mm:ssZ"
   *
   */
  public TimestampColumnTranslator() {
    setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
  }

  @Override
  public String translate(JdbcResultRow rs, int column) throws SQLException, IOException {
    Object fieldValue = rs.getFieldValue(column);
    
    if(fieldValue instanceof GregorianCalendar)
      fieldValue = ((GregorianCalendar) fieldValue).getTime();
    
    return toString((Date) fieldValue);
  }

  @Override
  public String translate(JdbcResultRow rs, String columnName) throws SQLException, IOException {
    Object fieldValue = rs.getFieldValue(columnName);
    
    if(fieldValue instanceof GregorianCalendar)
      fieldValue = ((GregorianCalendar) fieldValue).getTime();
    
    return toString((Date) fieldValue);
  }


}
