/*
 * Copyright 2017 Adaptris Ltd.
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

import java.sql.SQLException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.jdbc.JdbcResult;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Ignores the result set completely.
 * 
 * @config jdbc-noop-result-set-translator
 *
 */
@XStreamAlias("jdbc-noop-result-set-translator")
public class NoOpResultSetTranslator extends ResultSetTranslatorBase {

  @Override
  public void translate(JdbcResult source, AdaptrisMessage target) throws SQLException, ServiceException {
  }

}
