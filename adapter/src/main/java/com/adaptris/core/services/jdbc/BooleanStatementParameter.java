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

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang.BooleanUtils;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link Boolean} Statement Parameter.
 * 
 * <p>
 * {@code convert-null} has no meaning, empty/blank/whitespace only values will default to false.
 * </p>
 * @config jdbc-boolean-statement-parameter
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-boolean-statement-parameter")
@DisplayOrder(order = {"name", "queryString", "queryType"})
public class BooleanStatementParameter extends TypedStatementParameter {

  public BooleanStatementParameter() {
    super();
  }

  public BooleanStatementParameter(String query, QueryType type, Boolean nullConvert, String name) {
    super(query, type, nullConvert, name);
  }

  @Override
  public void apply(int parameterIndex, PreparedStatement statement, AdaptrisMessage msg) throws SQLException, ServiceException {
    Boolean bool = toBoolean(getQueryValue(msg));
    log.trace("Setting argument {} to [{}]", parameterIndex, bool);
    statement.setObject(parameterIndex, bool);
  }


  Boolean toBoolean(Object value) throws ServiceException {
    return Boolean.valueOf(BooleanUtils.toBoolean((String) value));
  }

  @Override
  public BooleanStatementParameter makeCopy() {
    return new BooleanStatementParameter(getQueryString(), getQueryType(), getConvertNull(), getName());
  }
}
