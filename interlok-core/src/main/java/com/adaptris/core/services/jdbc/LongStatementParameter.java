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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link Long} Statement Parameter.
 * 
 * <p>
 * If {@code convert-null} is true, then empty/blank/whitespace only values will default to 0.
 * </p>
 * 
 * @config jdbc-long-statement-parameter
 * @author lchan
 * 
 */
@JacksonXmlRootElement(localName = "jdbc-long-statement-parameter")
@XStreamAlias("jdbc-long-statement-parameter")
@DisplayOrder(order = {"name", "queryString", "queryType"})
public class LongStatementParameter extends TypedStatementParameter<Long> {

  public LongStatementParameter() {
    super();
  }


  public LongStatementParameter(String query, QueryType type, Boolean nullConvert, String name) {
    super(query, type, nullConvert, name);
  }

  @Override
  public LongStatementParameter makeCopy() {
    return new LongStatementParameter(getQueryString(), getQueryType(), getConvertNull(), getName());
  }

  @Override
  protected Long defaultValue() {
    return Long.valueOf(0);
  }

  @Override
  protected Long convertToType(Object value) {
    return Long.valueOf((String) value);
  }
}
