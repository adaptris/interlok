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

import org.apache.commons.lang.math.NumberUtils;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link Integer} Statement Parameter.
 * 
 * <p>
 * If {@code convert-null} is true, then empty/blank/whitespace only values will default to 0.
 * </p>
 * 
 * @config jdbc-integer-statement-parameter
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-integer-statement-parameter")
@DisplayOrder(order = {"name", "queryString", "queryType"})
public class IntegerStatementParameter extends TypedStatementParameter {

  public IntegerStatementParameter() {
    super();
  }

  public IntegerStatementParameter(String query, QueryType type, Boolean nullConvert, String name) {
    super(query, type, nullConvert, name);
  }

  @Override
  public void apply(int parameterIndex, PreparedStatement statement, AdaptrisMessage msg) throws SQLException, ServiceException {
    log.trace("Setting argument {} to [{}]", parameterIndex, getQueryValue(msg));
    statement.setObject(parameterIndex, toInteger(getQueryValue(msg)));
  }


  Integer toInteger(Object value) throws ServiceException {
    if (isBlank((String) value) && convertNull()) {
      return Integer.valueOf(NumberUtils.toInt((String) value));
    } else {
      return Integer.valueOf((String) value);
    }
  }

  @Override
  public IntegerStatementParameter makeCopy() {
    return new IntegerStatementParameter(getQueryString(), getQueryType(), getConvertNull(), getName());
  }

}
