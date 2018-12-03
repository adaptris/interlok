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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.adaptris.annotation.DisplayOrder;
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
public class TimestampStatementParameter extends DatetimeStatementParameter<Timestamp> {

  public TimestampStatementParameter() {
    super();
  }

  public TimestampStatementParameter(String query, QueryType type, SimpleDateFormat format) {
    this(query, type, null, null, format);
  }

  public TimestampStatementParameter(String query, QueryType type, Boolean nullConvert, String name, SimpleDateFormat format) {
    super(query, type, nullConvert, name, format);
  }

  public TimestampStatementParameter makeCopy() {
    return new TimestampStatementParameter(getQueryString(), getQueryType(), getConvertNull(), getName(), getFormatter());
  }

  @Override
  protected Timestamp defaultValue() {
    return new Timestamp(System.currentTimeMillis());
  }

  @Override
  protected Timestamp convertToType(Object value) {
    try {
      return new Timestamp(getFormatter().parse((String) value).getTime());
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Failed to convert input String [" + value + "] to [java.sql.Timestamp]", e);
    }
  }
}
