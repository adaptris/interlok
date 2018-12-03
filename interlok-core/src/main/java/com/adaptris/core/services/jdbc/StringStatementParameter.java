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

import com.adaptris.annotation.DisplayOrder;
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
public class StringStatementParameter extends TypedStatementParameter<String> {

  public StringStatementParameter() {
    super();
  }

  public StringStatementParameter(String query, QueryType type, Boolean nullConvert, String name) {
    super(query, type, nullConvert, name);
  }

  @Override
  public StringStatementParameter makeCopy() {
    return new StringStatementParameter(getQueryString(), getQueryType(), getConvertNull(), getName());
  }

  @Override
  protected String defaultValue() {
    return "";
  }

  @Override
  protected String convertToType(Object value) {
    return value.toString();
  }
}
