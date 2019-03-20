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
import javax.xml.namespace.NamespaceContext;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.w3c.dom.Node;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;

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
        return msg.getContent();
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
        // This query-type is *never* called via the jdbc-iterating-data-captures
        // those services will resolve the xpath directly and turn
        // things into a constant-param with the resolved value (null or otherwise).

        // This then, can only be called via JdbcRawCapture, so creating a new
        // document is fine, but *slow* if you have multiples of them
        // configured.
        return resolveXPath(msg, queryString);
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
  @AdvancedConfig
  @InputFieldDefault(value = "log everything")
  private ParameterLogger parameterLogger;

  public StatementParameterImpl() {
    
  }

  public StatementParameterImpl(String query, QueryType type, Boolean nullConvert, String name) {
    setQueryString(query);
    setQueryType(type);
    setConvertNull(nullConvert);
    setName(name);
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
    return BooleanUtils.toBooleanDefaultIfNull(getConvertNull(), false);
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

  public ParameterLogger getParameterLogger() {
    return parameterLogger;
  }

  /** Set the logger for non binary parameters.
   * 
   * @param parameterLogger
   */
  public void setParameterLogger(ParameterLogger parameterLogger) {
    this.parameterLogger = parameterLogger;
  }
  
  protected ParameterLogger logger() {
    return ObjectUtils.defaultIfNull(getParameterLogger(), (i, o) -> {
      log.trace("Setting argument {} to [{}]", i, o);
    });
  }

  public Object getQueryValue(AdaptrisMessage msg) {
    return getHandler(getQueryType()).getValue(msg, getQueryString());
  }

  protected QueryType getHandler(QueryType queryType) {
    return Args.notNull(queryType, "queryType");
  }

  private static String resolveXPath(AdaptrisMessage msg, String queryString) {
    // Might be null, if we're part of a RawJdbcDataCaptureService, but that
    // should be fine for XPath.newXPathInstance()
    DocumentBuilderFactoryBuilder builder = (DocumentBuilderFactoryBuilder) msg.getObjectHeaders()
        .get(JdbcDataQueryService.KEY_DOCBUILDER_FAC);
    NamespaceContext ctx = (NamespaceContext) msg.getObjectHeaders().get(JdbcDataQueryService.KEY_NAMESPACE_CTX);
    try {
      Node node = XPath.newXPathInstance(builder, ctx).selectSingleNode(XmlHelper.createDocument(msg, builder), queryString);
      return node != null ? node.getTextContent() : null;
    } catch (Exception e) {
      throw new IllegalArgumentException(queryString + " didn't work as an xpath");
    }
  }
}
