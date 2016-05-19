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

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.XmlUtils;

public abstract class StatementParameterImpl extends NamedStatementParameter {
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
  @NotNull
  private QueryType queryType;
  @InputFieldDefault(value = "false")
  @AdvancedConfig
  private Boolean convertNull;

  public StatementParameterImpl() {
    
  }

  public StatementParameterImpl(String query, QueryType type, Boolean nullConvert, String name) {
    setQueryString(query);
    setQueryType(type);
    setConvertNull(nullConvert);
  }


  public StatementParameterImpl(String query, QueryType type, Boolean nullConvert) {
    this(query, type, nullConvert, null);
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



  public Object getQueryValue(AdaptrisMessage msg) {
    return getHandler(getQueryType()).getValue(msg, getQueryString());
  }

  protected QueryType getHandler(QueryType queryType) {
    if (queryType == null) {
      throw new IllegalArgumentException(queryType + " not supported");
    }
    return queryType;
  }
}
