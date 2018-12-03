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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.security.exc.PasswordException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>AdaptrisConnectionImp</code> for JDBC.
 * </p>
 * 
 * @config jdbc-connection
 * 
 */
@XStreamAlias("jdbc-connection")
@AdapterComponent
@ComponentProfile(summary = "Connect to a database using a JDBC driver", tag = "connections,jdbc")
@DisplayOrder(order = {"username", "password", "driverImp", "connectUrl"})
public class JdbcConnection extends DatabaseConnection {

  private static final int NUM_SECONDS_TIMEOUT_CONN_VALIDATE = 5;
  
  private String connectUrl;
  private transient Connection sqlConnection;

  public JdbcConnection() {
    super();
  }

  /**
   * Convenience constructor.
   * 
   * @param url the URL.
   * @param driver the JDBC driver.
   */
  public JdbcConnection(String url, String driver) {
    this();
    setConnectUrl(url);
    setDriverImp(driver);
  }

  /**
   * @see com.adaptris.core.jdbc.DatabaseConnection#initialiseDatabaseConnection()
   */
  @Override
  protected void initialiseDatabaseConnection() throws CoreException {
    // To let us immediately test the connection, then let's try to do the connection.
    try {
      connect();
    }
    catch (SQLException e) {
      throw new CoreException(e);
    }
  }

  /**
   * @see com.adaptris.core.jdbc.DatabaseConnection#startDatabaseConnection()
   */
  @Override
  protected void startDatabaseConnection() throws CoreException {
  }

  /**
   * @see com.adaptris.core.jdbc.DatabaseConnection#stopDatabaseConnection()
   */
  @Override
  protected void stopDatabaseConnection() {
    if(sqlConnection instanceof ProxySqlConnection)
      ((ProxySqlConnection) sqlConnection).stop();
  }

  @Override
  protected void closeDatabaseConnection() {
    JdbcUtil.closeQuietly(sqlConnection);
    
    sqlConnection = null;
  }

  /** @see DatabaseConnection#makeConnection() */
  @Override
  protected Connection makeConnection() throws SQLException {
    validateConnection();

    return sqlConnection;
  }

  /**
   * <p>
   * Returns the connection string to use for this JDBC source.
   * </p>
   * 
   * @return the connection string to use for this JDBC source
   */
  public String getConnectUrl() {
    return connectUrl;
  }

  /**
   * <p>
   * Sets the connection string to use for this JDBC source.
   * </p>
   * 
   * @param s the connection string to use for this JDBC source
   */
  public void setConnectUrl(String s) {
    connectUrl = s;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (o instanceof JdbcConnection) {
      JdbcConnection rhs = (JdbcConnection) o;
      return new EqualsBuilder().append(getConnectUrl(), rhs.getConnectUrl())
          .append(getAlwaysValidateConnection(), rhs.getAlwaysValidateConnection()).append(getAutoCommit(), rhs.getAutoCommit())
          .append(getDebugMode(), rhs.getDebugMode()).append(getDriverImp(), rhs.getDriverImp())
          .append(getTestStatement(), rhs.getTestStatement()).isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(11, 17).append(getConnectUrl()).append(getAlwaysValidateConnection()).append(getAutoCommit())
        .append(getDebugMode()).append(getDriverImp()).append(getTestStatement()).toHashCode();
  }

  /** @see com.adaptris.core.jdbc.DatabaseConnection#getConnectionName() */
  @Override
  protected String getConnectionName() {
    return getConnectUrl();
  }

  /**
   * <p>
   * Validate the underlying connection.
   * </p>
   * 
   * @throws SQLException if we could not validate the connection.
   */
  private void validateConnection() throws SQLException {
    boolean connectionNeedsRefresh = false;
    try {
      connectionNeedsRefresh = ((sqlConnection == null) || (!sqlConnection.isValid(NUM_SECONDS_TIMEOUT_CONN_VALIDATE)));
    } catch (AbstractMethodError ex) {
      // ignore, some JDBC drivers throw an exception here for no apparent reason.
    }
    if (connectionNeedsRefresh) {
      try {
        Properties p = connectionProperties();
        sqlConnection = new ProxyNonClosingSqlConnection(DriverManager.getConnection(getConnectUrl(), p));
      }
      catch (PasswordException e) {
        sqlConnection = null;
        log.error("Couldn't decode password for database");
        throw new SQLException(e);
      }
    }
    try {
      if (alwaysValidateConnection()) {
        JdbcUtil.testConnection(sqlConnection, getTestStatement(), debugMode());
      }
    }
    catch (SQLException e) {
      sqlConnection = null;
      throw e;
    }
  }

}
