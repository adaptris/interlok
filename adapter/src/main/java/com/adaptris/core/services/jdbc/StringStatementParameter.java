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

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link String} Statement Parameter.
 * 
 * 
 * @config jdbc-string-statement-parameter
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-string-statement-parameter")
@DisplayOrder(order ={"name", "queryString", "queryType"})
public class StringStatementParameter extends TypedStatementParameter {

  public StringStatementParameter() {
    super();
  }

  public StringStatementParameter(String query, QueryType type, Boolean nullConvert, String name) {
    super(query, type, nullConvert, name);
  }

  @Override
  public void apply(int parameterIndex, PreparedStatement statement, AdaptrisMessage msg) throws SQLException, ServiceException {
    String val = toString(getQueryValue(msg));
    log.trace("Setting argument {} to [{}]", parameterIndex, val);
    statement.setString(parameterIndex, val);
  }

  String toString(Object value) throws ServiceException {
    String result;
    if (value == null) {
      if (convertNull()) {
        result = "";
      }
      else {
        result = null;
      }
    }
    else {
      result = value.toString();
    }
    return result;
  }

  @Override
  public StringStatementParameter makeCopy() {
    return new StringStatementParameter(getQueryString(), getQueryType(), getConvertNull(), getName());
  }
}
