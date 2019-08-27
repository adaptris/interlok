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

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link Boolean} Statement Parameter.
 * 
 * <p>
 * {@code convert-null} has no meaning, empty/blank/whitespace only values will default to false.
 * </p>
 * @config jdbc-boolean-statement-parameter
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-boolean-statement-parameter")
@DisplayOrder(order = {"name", "queryString", "queryType"})
public class BooleanStatementParameter extends TypedStatementParameter<Boolean> {

  public BooleanStatementParameter() {
    super();
  }

  public BooleanStatementParameter(String query, QueryType type, Boolean nullConvert, String name) {
    super(query, type, nullConvert, name);
  }

  @Override
  public BooleanStatementParameter makeCopy() {
    return new BooleanStatementParameter(getQueryString(), getQueryType(), getConvertNull(), getName());
  }

  @Override
  protected Boolean defaultValue() {
    return Boolean.FALSE;
  }

  @Override
  protected Boolean convertToType(Object o) {
    return Boolean.valueOf(BooleanUtils.toBoolean((String) o));
  }
}
