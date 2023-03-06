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

import com.adaptris.util.text.DateFormatUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import com.adaptris.core.util.Args;
import com.adaptris.jdbc.JdbcResultRow;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Column Translator implementation for handling date types
 *
 * @config jdbc-type-date-column-translator
 *
 */
@JacksonXmlRootElement(localName = "jdbc-type-date-column-translator")
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
    SimpleDateFormat sdf = DateFormatUtil.strictFormatter(getDateFormat());
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
    dateFormat = Args.notNull(format, "dateFormat");
  }

}
