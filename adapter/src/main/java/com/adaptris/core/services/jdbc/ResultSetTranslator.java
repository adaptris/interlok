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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.ComponentLifecycleExtension;
import com.adaptris.core.ServiceException;
import com.adaptris.jdbc.JdbcResult;

/**
 * Interface used to format output from a {@link JdbcDataQueryService}
 *
 * @author lchan
 *
 */
public interface ResultSetTranslator extends ComponentLifecycle, ComponentLifecycleExtension {

  /**
   * Translate the contents of the result set into the AdaptrisMessage object. Only use
   * this method for JdbcResults that are guaranteed to fit in memory.
   * 
   * @param source the result set from a database query executed by
   *          {@link JdbcDataQueryService}
   * @param target the adaptris message
   * @throws SQLException on errors accessing the result set.
   * @throws ServiceException wrapping any other exception
   */
  void translate(JdbcResult source, AdaptrisMessage target) throws SQLException, ServiceException;
  
}
