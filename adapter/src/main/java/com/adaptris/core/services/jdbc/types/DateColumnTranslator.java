package com.adaptris.core.services.jdbc.types;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import com.adaptris.jdbc.JdbcResultRow;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Column Translator implementation for handling date types
 * 
 * @config jdbc-type-date-column-translator
 * 
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-type-date-column-translator")
public class DateColumnTranslator implements ColumnTranslator {

  private String dateFormat;

  /**
   * The default dateformat is "yyyy-MM-dd"
   *
   */
  public DateColumnTranslator() {
    setDateFormat("yyyy-MM-dd");
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

  protected String toString(Date d) throws SQLException, IOException {
    SimpleDateFormat sdf = new SimpleDateFormat(getDateFormat());
    return sdf.format(d);
  }

  public String getDateFormat() {
    return dateFormat;
  }

  /**
   * Set the date format used to convert the column into a String.
   *
   * @param format the format; the default is "yyyy-MM-dd'T'HH:mm:ssZ".
   */
  public void setDateFormat(String format) {
    if (format == null) {
      throw new IllegalArgumentException("dateFormat is null");
    }
    dateFormat = format;
  }

}
