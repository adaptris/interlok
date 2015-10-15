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

import static org.apache.commons.lang.BooleanUtils.toBoolean;

import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link Boolean} Statement Parameter.
 * 
 * <p>
 * Note that this class pays no heed to the {@link StatementParameter#setQueryClass(String)} setting. Additionally,
 * {@link StatementParameter#setConvertNull(Boolean)} has no meaning; null/empty/whitespace values are implicitly false.
 * </p>
 * 
 * @config jdbc-boolean-statement-parameter
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-boolean-statement-parameter")
public class BooleanStatementParameter extends StatementParameter {

  public BooleanStatementParameter() {
    super();
    super.setQueryClass(null);
    super.setConvertNull(null);
  }

  public BooleanStatementParameter(String query, QueryType type) {
    super(query, (String) null, type);
  }

  @Override
  public Object convertToQueryClass(Object value) throws ServiceException {
    return Boolean.valueOf(toBoolean((String)value));
  }
}
