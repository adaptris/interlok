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

package com.adaptris.core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ProxyNonClosingSqlConnection is a simple wrapper around a SqlConnection
 * </p>
 * <p>
 * The only difference here is that calling close will not close the connection. Instead a trace logging message will show that we
 * are ignoring the close() method.
 * </p>
 * <p>
 * When you do actually want to close the connection, call stop().
 * </p>
 * 
 * @author Aaron
 * 
 */
public class ProxyNonClosingSqlConnection extends ProxySqlConnection {
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  
  public ProxyNonClosingSqlConnection(Connection c) {
    super(c);
  }

  /**
   * @see java.sql.Connection#close()
   */
  @Override
  public void close() throws SQLException {
    log.trace("Ignoring the connection.close() for a " + this.getClass().getSimpleName());
  }
 
}
