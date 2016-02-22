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

package com.adaptris.core.services.jdbc;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A {@link java.sql.Timestamp} extension to StatementParameter.
 * <p>
 * This ignores the query-class configuration, and always attempts to format the string into a java.sql.Timestamp using the
 * configured date formatter; if {@code convert-null} is true, then empty/blank/whitespace only values will be substituted by
 * {@link System#currentTimeMillis()}.
 * </p>
 * 
 * @config jdbc-timestamp-statement-parameter
 */
@XStreamAlias("jdbc-timestamp-statement-parameter")
@DisplayOrder(order = {"name", "queryString", "queryType", "dateFormat", "convertNull"})
public class TimestampStatementParameter extends TypedStatementParameter {
  @NotBlank
  private String dateFormat;

  private transient SimpleDateFormat dateFormatter = null;

  public TimestampStatementParameter() {
    super();
  }

  public TimestampStatementParameter(String query, QueryType type, SimpleDateFormat format) {
    this(query, type, null, null, format);
  }

  public TimestampStatementParameter(String query, QueryType type, Boolean nullConvert, String name, SimpleDateFormat format) {
    super(query, type, nullConvert, name);
    setDateFormat(format.toPattern());
  }

  @Override
  public void apply(int parameterIndex, PreparedStatement statement, AdaptrisMessage msg) throws SQLException, ServiceException {
    log.trace("Setting argument {} to [{}]", parameterIndex, getQueryValue(msg));
    statement.setObject(parameterIndex, this.toDate(getQueryValue(msg)));
  }


  /**
   * Set the format of the date.
   *
   * @param format the format of the date that is parse-able by SimpleDateFormat
   */
  public void setDateFormat(String format) {
    dateFormat = format;
  }

  /**
   * Get the configured Date Format.
   *
   * @return the date format.
   */
  public String getDateFormat() {
    return dateFormat;
  }

  SimpleDateFormat getFormatter() {
    if (dateFormatter == null) {
      dateFormatter = new SimpleDateFormat(getDateFormat());
    }
    return dateFormatter;
  }

  protected Object toDate(Object value) throws ServiceException {
    if (isBlank((String) value) && convertNull()) {
      return new java.sql.Timestamp(System.currentTimeMillis());
    }
    else {
      try {
        return new java.sql.Timestamp(getFormatter().parse((String) value).getTime());
      }
      catch (Exception e) {
        throw new ServiceException("Failed to convert input String [" + value + "] to type [java.sql.Timestamp]", e);
      }
    }
  }

  public TimestampStatementParameter makeCopy() {
    return new TimestampStatementParameter(getQueryString(), getQueryType(), getConvertNull(), getName(), getFormatter());
  }
}
