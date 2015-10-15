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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.util.JdbcUtil;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;

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
  private static final int START_OF_LIST = -1;
  private static final int MAX_DEPTH = 10;

  private transient Connection sqlConnection = null;
  private FailoverConfig config;
  private int currentIndex;
  private transient Logger logR = LoggerFactory.getLogger(this.getClass());

  private boolean isValid = true;

  private FailoverConnection() {
  }

  public FailoverConnection(FailoverConfig config) throws SQLException {
    this();
    setConfig(config);
    currentIndex = START_OF_LIST;
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
    if (sqlConnection != null) {
      sqlConnection.close();
    }
    isValid = false;
  }

  /**
   * Returns the userConnection.
   *
   * @return java.sql.Connection
   * @throws SQLException on error.
   */
  public synchronized Connection getConnection() throws SQLException {
    if (!isValid) {
      throw new SQLException("This connection is no longer valid.");
    }
    validateConnection(0);
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
    return;
  }

  private boolean createConnection() {
    boolean rc = false;
    rc = createConnection(currentIndex, config.getConnectionUrls().size());
    if (!rc && currentIndex > START_OF_LIST) {
      rc = createConnection(START_OF_LIST, currentIndex);
    }
    return rc;
  }

  private boolean createConnection(int start, int end) {
    List<String> list = config.getConnectionUrls();
    if (start >= list.size()) {
      return createConnection(START_OF_LIST, end);
    }
    Iterator i = list.listIterator(start == START_OF_LIST || start + 1 >= list.size() ? 0 : start + 1);
    while (i.hasNext()) {
      String url = i.next().toString();
      if (list.indexOf(url) > end) {
        break;
      }
      if (isDebugMode()) {
        logR.trace("Connection attempt to " + url);
      }
      try {
        sqlConnection = DriverManager.getConnection(url,
            mergeConnectionProperties(config.getConnectionProperties(), config.getUsername(), config.getPassword()));

        sqlConnection.setAutoCommit(config.getAutoCommit());
        currentIndex = list.indexOf(url);
        if (isDebugMode()) {
          logR.trace("Connected to [" + currentIndex + "] " + url);
        }
        return true;
      }
      catch (SQLException e) {
        logR.trace("Could not connect to " + url);
      }
      catch (PasswordException e) {
        logR.warn("Could not decode password");
        break;
      }
    }
    return false;
  }

  private void validateConnection(int currentDepth) throws SQLException {
    if (currentDepth > MAX_DEPTH) {
      throw new SQLException("Too much recursion, check test-statement for :  " + config.getConnectionUrls());
    }
    if (sqlConnection == null) {
      if (!this.createConnection()) {
        throw new SQLException("Could not create any jdbc connections to :- " + config.getConnectionUrls());
      }
    }
    try {
      testConnection();
    }
    catch (SQLException e) {
      logR.warn("Connection lost to [" + currentIndex + "] " + config.getConnectionUrls().get(currentIndex), e);
      sqlConnection = null;
      validateConnection(++currentDepth);
    }
    return;
  }

  private void testConnection() throws SQLException {
    Statement stmt = sqlConnection.createStatement();
    ResultSet rs = null;
    try {
      if (isEmpty(config.getTestStatement())) {
        return;
      }
      if (config.getAlwaysValidateConnection()) {
        if (isDebugMode()) {
          rs = stmt.executeQuery(config.getTestStatement());
          if (rs.next()) {
            StringBuffer sb = new StringBuffer("TestStatement Results - ");
            ResultSetMetaData rsm = rs.getMetaData();
            for (int i = 1; i <= rsm.getColumnCount(); i++) {
              sb.append("[");
              sb.append(rsm.getColumnName(i));
              sb.append("=");
              try {
                sb.append(rs.getString(i));
              }
              catch (Exception e) {
                sb.append("'unknown'");
              }
              sb.append("] ");
            }
            logR.trace(sb.toString());
          }
        }
        else {
          stmt.execute(config.getTestStatement());
        }
      }
    }
    finally {
      JdbcUtil.closeQuietly(rs);
      JdbcUtil.closeQuietly(stmt);
    }
  }

  private static Properties mergeConnectionProperties(Properties p, String username, String password) throws PasswordException {
    if (!isEmpty(username)) {
      p.setProperty("user", username);
    }
    if (!isEmpty(password)) {
      p.setProperty("password", Password.decode(password));
    }
    return p;
  }
}
