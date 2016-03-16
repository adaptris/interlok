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

import java.sql.SQLException;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.jdbc.JdbcResult;

/**
 * Interface used to format output from a {@link SplittingJdbcDataQueryService}
 *
 * @author gdries
 */
public interface SplittingResultSetTranslator extends AdaptrisComponent {

  /**
   * Translate the JdbcResult into (possibly) multiple AdaptrisMessage objects. This method should be
   * used when the JdbcResult may be too big for a single message (or too big for RAM) or if you want
   * to split the JDBC result into multiple messages (x rows per message, for example).
   * 
   * @param source
   * @return an Iterable that may result in multiple AdaptrisMessage objects
   * @throws SQLException
   * @throws ServiceException
   */
  Iterable<AdaptrisMessage> translate(JdbcResult source) throws SQLException, ServiceException;
  
}
