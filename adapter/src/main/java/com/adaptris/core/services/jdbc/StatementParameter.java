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

import java.lang.reflect.Constructor;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Encapsulates a parameter that is used by the JdbcDataCaptureService.
 * 
 * @config jdbc-statement-parameter
 * 
 * @author sellidge
 */
@XStreamAlias("jdbc-statement-parameter")
public class StatementParameter extends StatementParameterImpl {

  private String queryClass;

  public StatementParameter() {
  }

  public StatementParameter(String query, Class<?> clazz, QueryType type) {
    this(query, clazz, type, null);
  }

  public StatementParameter(String query, String classname, QueryType type) {
    this(query, classname, type, null);
  }

  public StatementParameter(String query, String classname, QueryType type, Boolean nullConvert) {
    this(query, classname, type, nullConvert, null);
  }

  public StatementParameter(String query, String classname, QueryType type, Boolean nullConvert, String paramName) {
    super(query, type, nullConvert, paramName);
    setQueryClass(classname);
  }
  
  public StatementParameter(String query, Class<?> clazz, QueryType type, Boolean nullConvert) {
    this(query, clazz.getName(), type, nullConvert);
  }


  @Override
  public void apply(int parameterIndex, PreparedStatement statement, AdaptrisMessage msg) throws SQLException, ServiceException {
    log.trace("Setting argument {} to [{}]", parameterIndex, getQueryValue(msg));
    statement.setObject(parameterIndex, this.convertToQueryClass(getQueryValue(msg)));
  }

  /**
   * The type of underlying jdbc object.
   *
   * @param clazz the clas type.
   */
  public void setQueryClass(String clazz) {
    queryClass = clazz;
  }

  /**
   * Return the type of the underlying jdbc object.
   *
   * @return the type.
   */
  public String getQueryClass() {
    return queryClass;
  }

  /**
   * Convert the given string to the corresponding query class.
   *
   * @param value the string obtained.
   * @return an Object suitable for use in the service.
   * @throws ServiceException on error.
   */
  protected Object convertToQueryClass(Object value) throws ServiceException {
    if (value == null && convertNull()) {
      return "";
    }
    else {
      if(value instanceof String) {
        value = (String) value;
        try {
          Class<?> clazz = Class.forName(queryClass);
          Object obj = null;
  
          Constructor<?> construct = clazz.getConstructor(new Class[]
          {
            String.class
          });
          obj = construct.newInstance(new Object[]
          {
            value
          });
  
          return obj;
        }
        catch (Exception e) {
          throw new ServiceException("Failed to convert input String [" + value
              + "] to type [" + queryClass + "]", e);
        }
      } else
        return value; // assume we already have the correct type, no conversion needed
    }
  }

  public StatementParameter makeCopy() {
    return new StatementParameter(getQueryString(), getQueryClass(), getQueryType(), getConvertNull(), getName());
  }
}
