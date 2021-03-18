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

package com.adaptris.jdbc.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.security.exc.PasswordException;

/**
 * A wrapper around a JDBC Connection. Supports failover configuration, but can
 * be used with a single database connection.
 * <p>
 * The configured FailoverConfig#getTestStatement will be used to test the
 * current connection. If the statement fails, then a reconnection attempt is
 * made (provided there are available connection urls to try).
 * </p>
 *
 * @see FailoverConfig
 * @author lchan
 * @author $Author: lchan $
 */
public final class FailoverConnection {

  private transient Connection sqlConnection = null;
  private transient FailoverConfig config;
  private transient Logger logR = LoggerFactory.getLogger(this.getClass());

  private FailoverConnection() {
  }

  public FailoverConnection(FailoverConfig config) throws SQLException {
    this();
    setConfig(config);
    if (config.getConnectionUrls().size() < 1) {
      throw new SQLException("No Configured URL list for connections");
    }
    initialiseDriver();
  }

  /**
   * Get the configuration.
   */
  public FailoverConfig getConfig() {
    return config;
  }

  /**
   * Set the configuration.
   *
   * @param config the configuration.
   */
  public void setConfig(FailoverConfig config) {
    this.config = config;
  }

  /**
   * Close the connection.
   * <p>
   * If the connection is closed, then any attempt to get the underlying
   * connection will throw a SQLException
   *
   * @throws SQLException on error
   */
  public void close() throws SQLException {
    JdbcUtil.closeQuietly(sqlConnection);
    sqlConnection = null;
  }

  /**
   * Returns the userConnection.
   *
   * @return java.sql.Connection
   * @throws SQLException on error.
   */
  public synchronized Connection getConnection() throws SQLException {
    validateConnection();
    return sqlConnection;
  }

  /**
   * Is this connection in debug mode.
   *
   * @return true or false.
   */
  public boolean isDebugMode() {
    return config.getDebugMode();
  }

  private void initialiseDriver() throws SQLException {
    try {
      Class.forName(config.getDatabaseDriver());
    }
    catch (ClassNotFoundException e) {
      throw new SQLException(config.getDatabaseDriver() + " not found");
    }
  }

  private void createConnection() throws SQLException {
    List<String> list = config.getConnectionUrls();
    for (String url : list) {
      try {
        if (isDebugMode()) {
          logR.trace("Connection attempt to [{}]", url);
        }
        sqlConnection = DriverManager.getConnection(url,
            JdbcUtil.mergeConnectionProperties(config.getConnectionProperties(), config.getUsername(), config.getPassword()));
        sqlConnection.setAutoCommit(config.getAutoCommit());
        if (isDebugMode()) {
          logR.trace("Connected to [{}]", url);
        }
        return;
      } catch (SQLException e) {
        logR.warn("Could not connect to {}", url);
      } catch (PasswordException e) {
        logR.warn("Could not decode password");
        break;
      }
    }
    throw new SQLException("Could not create any jdbc connections to :- " + config.getConnectionUrls());
  }

  private void validateConnection() throws SQLException {
    try {
      if (sqlConnection == null) {
        createConnection();
      }
      JdbcUtil.testConnection(sqlConnection, config.getAlwaysValidateConnection());
    }
    catch (SQLException e) {
      sqlConnection = null;
      throw e;
    }
  }
}
