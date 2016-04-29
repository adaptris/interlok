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

package com.adaptris.core.services.metadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Performs a Regular Expression based Query on a supplied String.
 * </p>
 * 
 * @config regexp-metadata-query
 */
@XStreamAlias("regexp-metadata-query")
public class RegexpMetadataQuery {

  private String metadataKey = null;
  private String queryExpression = null;
  private Boolean allowNulls;
  private transient Pattern pattern = null;

  private transient Logger logR = LoggerFactory.getLogger(this.getClass());

  public RegexpMetadataQuery() {
  }

  public RegexpMetadataQuery(String key, String query) {
    this();
    setQueryExpression(query);
    setMetadataKey(key);
  }

  /**
   * <p>
   * Performs the query against the payload of the supplied String and
   * constructs a MetdataElement with the configured Key and the result as the
   * Value.
   * </p>
   * @param message the String to run the Query on
   * @return a MetadataElement with the configured Key and the result of the
   * Query as it's Value <b>NOTE: the Value of the MetadataElement will be null
   * if nulls have been allowed</b>
   * @throws CoreException wrapping any underlying Exception
   */
  public synchronized MetadataElement doQuery(String message)
    throws CoreException {
    if (pattern == null) {
      try {
        pattern = Pattern.compile(queryExpression);
      }
      catch (Exception e) {
        throw new CoreException(
          "Failed to create Query [" + e.getMessage() + "]",
          e);
      }
    }

    Matcher matcher = pattern.matcher(message);

    MetadataElement elem = new MetadataElement();
    elem.setKey(metadataKey);

    if (matcher.find()) {
      String value = matcher.group(1);
      elem.setValue(value);
    }
    else {
      if (!allowNullResults()) {
        throw new CoreException(
          "Failed to match pattern [" + metadataKey + "] to input string");
      }
    }

    return elem;
  }

  /**
   * <p>
   * Sets the key to store the result of the regexp query against.
   * </p>
   * @param s the key to store the result of the regexp query against
   */
  public void setMetadataKey(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException();
    }
    else {
      metadataKey = s;
    }
  }

  /**
   * <p>
   * Returns the key to store the result of the regexp query against.
   * </p>
   * @return the key to store the result of the regexp query against
   */
  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * <p>
   * Sets the regexp query expression to use.
   * </p>
   * @param s the regexp query expression to use
   */
  public void setQueryExpression(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException();
    }
    else {
      queryExpression = s;
    }
  }

  /**
   * <p>
   * Returns the regexp query expression.
   * </p>
   * @return the regexp query expression
   */
  public String getQueryExpression() {
    return queryExpression;
  }

  /**
   * <p>
   * Tells the class whether to allow processing to continue after
   * performing an regexp which returns no elements. Default is false
   * <b>WARNING! if using this method, the returned MetadataElements
   * will return null from their getValue() clause. Ensure the receiving
   * service is able to cater for this eventuality</b>
   * </p>
   * @param b set whether or not to process
   */
  public void setAllowNulls(Boolean b) {
    allowNulls = b;
  }

  /**
   * <p>
   * Returns whether the class allows null results.
   * </p>
   * @return whether the class allows null results
   */
  public Boolean getAllowNulls() {
    return allowNulls;
  }

  boolean allowNullResults() {
    return getAllowNulls() != null ? getAllowNulls().booleanValue() : false;
  }
}
