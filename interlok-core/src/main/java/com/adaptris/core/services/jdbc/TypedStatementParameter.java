/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
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

import org.apache.commons.lang3.StringUtils;

import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;

/**
 * Abstract class preserving backwards config compatibility from {@link StatementParameter}.
 * 
 * @author lchan
 *
 */
public abstract class TypedStatementParameter<T> extends StatementParameterImpl {

  // ~This is just here to avoid @XStream unmarshalling errors, due to the hierarchy change.
  // Should never be output as it should be null; doesn't have a getter/setter so the UI doesn't know about it.
  // Eventually we can remove this and sub-classes can just extend StatementParameterImpl directly.
  // This class was introduced in 3.2.0...
  @Removal(version="3.9")
  private String queryClass;

  public TypedStatementParameter() {
    super();
  }

  public TypedStatementParameter(String query, QueryType type, Boolean nullConvert, String name) {
    super(query, type, nullConvert, name);
  }

  @Override
  public final void apply(int parameterIndex, PreparedStatement statement, AdaptrisMessage msg)
      throws SQLException {
    Object o = getQueryValue(msg);
    T typedParam = convert(o);
    logger().log(parameterIndex, typedParam);   
    statement.setObject(parameterIndex, typedParam);
  }

  protected T convert(Object value) {
    T result = null;
    if (consideredNull(value)) {
      if (convertNull()) {
        result = defaultValue();
      }
      else {
        result = (T) value;
      }
    }
    else {
      result = convertToType(value);
    }
    return result;
  }

  private boolean consideredNull(Object o) {
    if (o == null) {
      return true;
    }
    else if (o instanceof String) {
      return StringUtils.isBlank((String) o);
    }
    return false;
  }

  protected abstract T defaultValue();

  protected abstract T convertToType(Object value);

}
