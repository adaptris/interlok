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



/**
 * Abstract class for translating result sets into metadata.
 *
 * @author lchan
 *
 */
public abstract class MetadataResultSetTranslatorImpl extends ResultSetTranslatorImp {

  protected static final String DEFAULT_METADATA_KEY = "JdbcDataQueryServiceOutput";

  private String metadataKeyPrefix;
  private String separator;
  private String resultSetCounterPrefix;

  public MetadataResultSetTranslatorImpl() {
    super();
    setMetadataKeyPrefix(DEFAULT_METADATA_KEY);
    setSeparator("_");
  }

  public String getMetadataKeyPrefix() {
    return metadataKeyPrefix;
  }

  /**
   * Set the metadata key prefix for each metadata key generated.
   *
   * @param s
   */
  public void setMetadataKeyPrefix(String s) {
    metadataKeyPrefix = s;
  }

  public String getSeparator() {
    return separator;
  }

  /**
   * Set the separator between the prefix and the generated column name.
   *
   * @param s
   */
  public void setSeparator(String s) {
    separator = s;
  }

  public String getResultSetCounterPrefix() {
    return resultSetCounterPrefix;
  }

  public void setResultSetCounterPrefix(String resultSetCounterPrefix) {
    this.resultSetCounterPrefix = resultSetCounterPrefix;
  }
}
