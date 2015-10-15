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

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.util.XmlUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Encapsulates a parameter that is used by the JdbcDataCaptureService.
 * 
 * @config jdbc-statement-parameter
 * 
 * @author sellidge
 */
@XStreamAlias("jdbc-statement-parameter")
public class StatementParameter implements JdbcStatementParameter {

  /**
   * Defines all the static query types supported by a Statement Parameter.
   * 
   * 
   */
  public static enum QueryType {
    payload {
      @Override
      String getValue(AdaptrisMessage msg, String queryString) {
        return msg.getStringPayload();
      }
    },
    metadata {
      @Override
      String getValue(AdaptrisMessage msg, String queryString) {
        return msg.getMetadataValue(queryString);
      }
    },
    xpath {
      @Override
      String getValue(AdaptrisMessage msg, String queryString) {
        XmlUtils xu = (XmlUtils) msg.getObjectMetadata().get(JdbcDataQueryService.KEY_XML_UTILS);
        return xu.getSingleTextItem(queryString, xu.getSingleNode("/"));

      }
    },
    constant {
      @Override
      String getValue(AdaptrisMessage msg, String queryString) {
        return queryString;
      }
    },
    id {
      @Override
      String getValue(AdaptrisMessage msg, String queryString) {
        return msg.getUniqueId();
      }
    };
    abstract Object getValue(AdaptrisMessage msg, String queryString);
  }


  private String queryString;
  private String queryClass;
  @NotNull
  private QueryType queryType;
  private Boolean convertNull;
  private String name;


  protected transient Logger logR = LoggerFactory.getLogger(this.getClass());

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
    this();
    setQueryString(query);
    setQueryClass(classname);
    setQueryType(type);
    setConvertNull(nullConvert);
    setName(paramName);
  }
  
  public StatementParameter(String query, Class<?> clazz, QueryType type, Boolean nullConvert) {
    this(query, clazz.getName(), type, nullConvert);
  }

  public Object getQueryValue(AdaptrisMessage msg) {
    return getHandler(getQueryType()).getValue(msg, getQueryString());
  }

  private QueryType getHandler(QueryType queryType) {
    if (queryType == null) {
      throw new IllegalArgumentException(queryType + " not supported");
    }
    return queryType;
  }
  
  @Override
  public void apply(int parameterIndex, PreparedStatement statement, AdaptrisMessage msg) throws SQLException, ServiceException {
    statement.setObject(parameterIndex, this.convertToQueryClass(getQueryValue(msg)));
  }

  /**
   * Defines a method how to get data out of the payload.
   * <p>
   * The configured query string is tied to the underlying querytype; so for 'payload' it could be null, for 'metadata' it would be
   * a metadata key, and for 'xpath' a valid XPath Expression.
   * </p>
   * 
   * @see #setQueryType(QueryType)
   * @param s the query.
   */
  public void setQueryString(String s) {
    queryString = s;
  }

  /**
   * Get the query string.
   *
   * @return the string.
   */
  public String getQueryString() {
    return queryString;
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
   * The type of query this is.
   *
   * @param queryType the type.
   */
  public void setQueryType(QueryType queryType) {
    this.queryType = queryType;
  }

  /**
   * Return the query type.
   *
   * @return the query type.
   */
  public QueryType getQueryType() {
    return queryType;
  }

  /**
   * Convert the given string to the corresponding query class.
   *
   * @param value the string obtained.
   * @return an Object suitable for use in the service.
   * @throws ServiceException on error.
   */
  public Object convertToQueryClass(Object value) throws ServiceException {
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

  /**
   * Whether to convert null object results into something meaningful.
   *
   * @return true or false.
   */
  public Boolean getConvertNull() {
    return convertNull;
  }

  /**
   * Set whether to convert null objects into something meaningful.
   * <p>
   * If set to true, then this class will convert null parameters into an empty string. Other sub-classes may perform different
   * types of conversion
   * </p>
   * 
   * @param b true to convert a null object into an empty string, default null (false)
   */
  public void setConvertNull(Boolean b) {
    convertNull = b;
  }

  protected boolean convertNull() {
    return getConvertNull() != null ? getConvertNull().booleanValue() : false;
  }

  public String getName() {
    return name;
  }

  /**
   * Set the name to be associated with this parameter.
   * <p>
   * The name is only used if you are using a {@link NamedParameterApplicator} to apply parameters to your JDBC statement.
   * </p>
   * 
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

}
