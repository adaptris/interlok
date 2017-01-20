/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.core.jdbc;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;
import javax.validation.Valid;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.security.password.Password;
import com.adaptris.util.KeyValuePairSet;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A {@link DatabaseConnection} instance that provides connection pooling via c3p0.
 * 
 * @author amcgrath
 * @see PooledConnectionProperties
 */
@XStreamAlias("advanced-jdbc-pooled-connection")
@AdapterComponent
@ComponentProfile(summary = "Connect to a database using a JDBC driver; connection pooling handled via C3P0",
    tag = "connections,jdbc")
@DisplayOrder(order = {"username", "password", "driverImp", "connectUrl", "connectionPoolProperties", "connectionProperties"})
public class AdvancedJdbcPooledConnection extends DatabaseConnection {
  
  @NotBlank
  private String connectUrl;
  
  @Valid
  private KeyValuePairSet connectionPoolProperties;
  
  private transient ComboPooledDataSource connectionPool;
  
  public AdvancedJdbcPooledConnection() {
    super();
  }
  
  @Override
  protected Connection makeConnection() throws SQLException {
    Connection sqlConnection = connectionPool.getConnection();
    
    this.validateConnection(sqlConnection);
    return sqlConnection;
  }

  @Override
  protected void initialiseDatabaseConnection() throws CoreException {
    initPool();
  }
  
  private synchronized void initPool() throws CoreException {
    try {
      connectionPool = new ComboPooledDataSource();
      connectionPool.setProperties(connectionProperties());
      connectionPool.setDriverClass(this.getDriverImp());
      connectionPool.setJdbcUrl(this.getConnectUrl());
      connectionPool.setUser(this.getUsername());
      connectionPool.setPassword(Password.decode(this.getPassword()));

      connectionPool.setAcquireRetryDelay(Long.valueOf(connectionRetryInterval()).intValue());
      connectionPool.setAcquireRetryAttempts(connectionAttempts());
      connectionPool.setTestConnectionOnCheckin(alwaysValidateConnection());
      connectionPool.setTestConnectionOnCheckout(alwaysValidateConnection());
      PooledConnectionProperties.apply(getConnectionPoolProperties(), connectionPool);
    }
    catch (Exception ex) {
      throw new CoreException(ex);
    }
  }

  @Override
  protected void startDatabaseConnection() throws CoreException {
  }

  @Override
  protected void stopDatabaseConnection() {
  }

  @Override
  protected void closeDatabaseConnection() {
    if (connectionPool != null) {
      connectionPool.close();
    }
  }

  @Override
  public DataSource asDataSource() throws SQLException {
    if(connectionPool == null) {
      try {
        initPool();
      } catch (CoreException ex) {
        throw new SQLException(ex);
      }
    }
    return connectionPool;
  }
  
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
  private void validateConnection(Connection sqlConnection) throws SQLException {
    try {
      if (alwaysValidateConnection())  testConnection(sqlConnection);
    }
    catch (SQLException e) {
      throw e;
    }
  }

  /**
   * <p>
   * Run the test statement against the database.
   * </p>
   * 
   * @throws SQLException if the statement could not be performed.
   */
  private void testConnection(Connection sqlConnection) throws SQLException {
    if (isEmpty(getTestStatement())) {
      log.trace("No Test Statement, we will not test the JDBC connection.");
      return;
    }
    Statement stmt = sqlConnection.createStatement();
    ResultSet rs = null;
    try {
      if (debugMode()) {
        rs = stmt.executeQuery(getTestStatement());
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
          log.trace(sb.toString());
        }
      }
      else {
        stmt.execute(getTestStatement());
      }
    }
    finally {
      JdbcUtil.closeQuietly(rs);
      JdbcUtil.closeQuietly(stmt);
    }
  }

  @Override
  public boolean equals(Object ajpc) {
    if (ajpc == null) 
      return false;
    
    if (ajpc == this) 
      return true;
    
    if (ajpc instanceof AdvancedJdbcPooledConnection) {
      AdvancedJdbcPooledConnection conn = (AdvancedJdbcPooledConnection) ajpc;
      
      return new EqualsBuilder()
          .append(conn.getConnectUrl(), this.getConnectUrl())
          .append(conn.getDriverImp(), this.getDriverImp())
          .append(conn.getAlwaysValidateConnection(), this.getAlwaysValidateConnection())
          .append(conn.getDebugMode(), this.getDebugMode())
          .append(conn.getTestStatement(), this.getTestStatement())
          .append(conn.getAutoCommit(), this.getAutoCommit())
          .append(conn.getConnectionProperties(), this.getConnectionProperties())
          .append(conn.getConnectionPoolProperties(), this.getConnectionPoolProperties())
          .isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 31)
        .append(this.getConnectUrl())
        .append(getDriverImp())
        .append(this.getAlwaysValidateConnection())
        .append(getDebugMode())
        .append(getTestStatement())
        .append(getAutoCommit())
        .append(this.getConnectionProperties())
        .append(getConnectionPoolProperties())
        .toHashCode();
  }

  public String getConnectUrl() {
    return connectUrl;
  }

  public void setConnectUrl(String connectUrl) {
    this.connectUrl = connectUrl;
  }

  public KeyValuePairSet getConnectionPoolProperties() {
    return connectionPoolProperties;
  }

  public void setConnectionPoolProperties(KeyValuePairSet kvps) {
    this.connectionPoolProperties = kvps;
  }

  public int currentBusyConnectionCount() throws SQLException {
    return connectionPool.getNumBusyConnections();
  }

  public int currentConnectionCount() throws SQLException {
    return connectionPool.getNumConnections();
  }

  public int currentIdleConnectionCount() throws SQLException {
    return connectionPool.getNumIdleConnections();
  }


}
