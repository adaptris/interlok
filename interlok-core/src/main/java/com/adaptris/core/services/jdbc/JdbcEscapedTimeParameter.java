/*
 * Copyright 2018 Adaptris Ltd.
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

import java.sql.Time;

import com.adaptris.annotation.DisplayOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A {@link java.sql.Time} extension to StatementParameter.
 * <p>
 * This ignores the query-class configuration, and always attempts to format the string into a java.sql.Time using
 * {@link java.sql.Time#valueOf(String)}; if {@code convert-null} is true, then empty/blank/whitespace only values will be
 * substituted by {@link System#currentTimeMillis()}.
 * </p>
 * 
 * @config jdbc-escaped-time-statement-parameter
 * 
 */
@JacksonXmlRootElement(localName = "jdbc-escaped-time-statement-parameter")
@XStreamAlias("jdbc-escaped-time-statement-parameter")
@DisplayOrder(order = {"name", "queryString", "queryType", "convertNull"})
public class JdbcEscapedTimeParameter extends TypedStatementParameter<Time> {

  public JdbcEscapedTimeParameter() {
    super();
  }

  public JdbcEscapedTimeParameter(String query, QueryType type) {
    this(query, type, null, null);
  }

  public JdbcEscapedTimeParameter(String query, QueryType type, Boolean nullConvert, String name) {
    super(query, type, nullConvert, name);
  }

  @Override
  public JdbcEscapedTimeParameter makeCopy() {
    return new JdbcEscapedTimeParameter(getQueryString(), getQueryType(), getConvertNull(), getName());
  }

  @Override
  protected Time defaultValue() {
    return new Time(System.currentTimeMillis());
  }

  @Override
  protected Time convertToType(Object value) {
    try {
      return Time.valueOf(value.toString());
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Failed to convert input String [" + value + "] to type [java.sql.Time]", e);
    }
  }
}
