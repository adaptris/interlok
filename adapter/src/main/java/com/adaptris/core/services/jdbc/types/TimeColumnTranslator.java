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
import java.util.Date;
import java.util.GregorianCalendar;

import com.adaptris.jdbc.JdbcResultRow;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Column Translator implementation for handling time types
 * 
 * @config jdbc-type-time-column-translator
 * 
 * 
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-type-time-column-translator")
public class TimeColumnTranslator extends DateColumnTranslator {

  /**
   * The default dateformat is "HH:mm:ssZ"
   *
   */
 public TimeColumnTranslator() {
    setDateFormat("HH:mm:ssZ");
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
