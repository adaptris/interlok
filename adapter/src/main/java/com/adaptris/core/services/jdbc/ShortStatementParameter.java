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
 * {@link Short} Statement Parameter.
 * 
 * <p>
 * If {@code convert-null} is true, then empty/blank/whitespace only values will default to 0.
 * </p>
 * 
 * @config jdbc-short-statement-parameter
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-short-statement-parameter")
@DisplayOrder(order = {"name", "queryString", "queryType"})
public class ShortStatementParameter extends TypedStatementParameter<Short> {

  public ShortStatementParameter() {
    super();
  }

  public ShortStatementParameter(String query, QueryType type, Boolean nullConvert, String name) {
    super(query, type, nullConvert, name);
  }

  @Override
  public ShortStatementParameter makeCopy() {
    return new ShortStatementParameter(getQueryString(), getQueryType(), getConvertNull(), getName());
  }

  @Override
  protected Short defaultValue() {
    return Short.valueOf((short) 0);
  }

  @Override
  protected Short convertToType(Object value) {
    return Short.valueOf((String) value);
  }
}
