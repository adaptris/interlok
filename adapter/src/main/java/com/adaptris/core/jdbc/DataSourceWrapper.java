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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * Datasource that exposes DatabaseConnection as a datasource.
 * <p>
 * Because DatabaseConnection is configured explicitly with the username and
 * password, the implementation of
 * <code>getConnection(String username, String password)</code> simply discards
 * the username and password and relies on the underlying configuration. This is
 * the desired behaviour.
 * </p>
 * <p>
 * Calling <code>getConnection()</code> makes no guarantee that the underlying
 * DatabaseConnection has been initialised or started
 * </p>
 * , it is expected the the normal adapter lifecycle takes care of this. </p>
 *
 * @see DataSource
 * @see DriverManager#getLogWriter()
 * @see DatabaseConnection
 * @author lchan
 */
class DataSourceWrapper implements DataSource {

  private transient DatabaseConnection dbc = null;

  private DataSourceWrapper() {
  }

  DataSourceWrapper(DatabaseConnection c) {
    this();
    dbc = c;
  }

  /**
   * This method is a stub and has no effect.
   *
   * @see javax.sql.DataSource#getLoginTimeout()
   */
  @Override
  public int getLoginTimeout() throws SQLException {
    return 0;
  }

  /**
   * This method is a stub and has no effect.
   *
   * @see javax.sql.DataSource#setLoginTimeout(int)
   */
  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
  }

  /**
   * This simply uses the static log writer from the DriverManager class.
   *
   * @see javax.sql.DataSource#getLogWriter()
   */
  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return DriverManager.getLogWriter();
  }

  /**
   * This simply uses the static log writer from the DriverManager class.
   *
   * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
   */
  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    DriverManager.setLogWriter(out);
  }

  /**
   *
   * @see javax.sql.DataSource#getConnection()
   */
  @Override
  public Connection getConnection() throws SQLException {
    return new ProxyNonClosingSqlConnection(dbc.connect());
  }

  /**
   * Connect using a specific username and password.
   * <p>
   * Because DatabaseConnection is configured explicitly with the username and
   * password, the implementation of
   * <code>getConnection(String username, String password)</code> simply
   * discards the username and password and relies on the underlying
   * configuration. This is the desired behaviour.
   * </p> *
   *
   * @see javax.sql.DataSource#getConnection(String, String)
   */
  @Override
  public Connection getConnection(String username, String password)
      throws SQLException {
    return getConnection();
  }

  /**
   * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
   */
  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }

  /**
   * @see java.sql.Wrapper#unwrap(java.lang.Class)
   */
  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new SQLException("Can't unwrap to " + iface);
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    // TODO Auto-generated method stub
    return null;
  }

}
