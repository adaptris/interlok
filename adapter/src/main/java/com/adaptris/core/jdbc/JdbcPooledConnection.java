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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;
import javax.validation.Valid;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.security.password.Password;
import com.adaptris.util.TimeInterval;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("jdbc-pooled-connection")
@AdapterComponent
@ComponentProfile(summary = "Connect to a database using a JDBC driver; connection pooling handled via C3P0",
    tag = "connections,jdbc")
@DisplayOrder(order = {"username", "password", "driverImp", "connectUrl", "minimumPoolSize", "maximumPoolSize", "maxIdleTime",
    "acquireIncrement"})
public class JdbcPooledConnection extends DatabaseConnection {
  
  private static final int DEFAULT_MINIMUM_POOL_SIZE = 5;
  private static final int DEFAULT_MAXIMUM_POOL_SIZE = 50;
  private static final int DEFAULT_ACQUIRE_INCREMENT = 5;
  
  private static final TimeInterval DEFAULT_CONN_ACQUIRE_WAIT = new TimeInterval(5L, TimeUnit.SECONDS);
  private static final TimeInterval DEFAULT_IDLE_TEST_PERIOD = new TimeInterval(60L, TimeUnit.SECONDS);
  private static final TimeInterval DEFAULT_MAX_IDLE_TIME = new TimeInterval(10L, TimeUnit.MINUTES);

  private transient ComboPooledDataSource connectionPool;
  
  @NotBlank
  private String connectUrl;
  
  private int minimumPoolSize;
  private int maximumPoolSize;
  @AdvancedConfig
  private int acquireIncrement;
  @Valid
  @AdvancedConfig
  private TimeInterval connectionAcquireWait;
  @Valid
  @AdvancedConfig
  private TimeInterval idleConnectionTestPeriod;
  @Valid
  @AdvancedConfig
  private TimeInterval maxIdleTime;
  
  public JdbcPooledConnection () {
    super();
    this.setMinimumPoolSize(DEFAULT_MINIMUM_POOL_SIZE);
    this.setMaximumPoolSize(DEFAULT_MAXIMUM_POOL_SIZE);
    this.setAcquireIncrement(DEFAULT_ACQUIRE_INCREMENT);
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

      connectionPool.setMinPoolSize(this.getMinimumPoolSize());
      connectionPool.setAcquireIncrement(this.getAcquireIncrement());
      connectionPool.setMaxPoolSize(this.getMaximumPoolSize());
      connectionPool.setCheckoutTimeout(connectionAcquireWait());
      connectionPool.setAcquireRetryDelay(Long.valueOf(connectionRetryInterval()).intValue());
      connectionPool.setAcquireRetryAttempts(connectionAttempts());
      connectionPool.setTestConnectionOnCheckin(alwaysValidateConnection());
      connectionPool.setTestConnectionOnCheckout(alwaysValidateConnection());
      connectionPool.setIdleConnectionTestPeriod(idleConnectionTestPeriod());
      connectionPool.setMaxIdleTime(maxIdleTime());
    }
    catch (Exception ex) {
      throw new CoreException(ex);
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
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (o instanceof JdbcPooledConnection) {
      JdbcPooledConnection pooledConnection = (JdbcPooledConnection) o;
      
      return new EqualsBuilder().append(pooledConnection.getConnectUrl(), this.getConnectUrl())
                                .append(pooledConnection.getDriverImp(), this.getDriverImp())
                                .append(pooledConnection.getMinimumPoolSize(), this.getMinimumPoolSize())
                                .append(pooledConnection.getMaximumPoolSize(), this.getMaximumPoolSize())
          .append(pooledConnection.getAcquireIncrement(), this.getAcquireIncrement()).isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(19, 31).append(this.getConnectUrl()).append(this.getMinimumPoolSize()).append(this.getMaximumPoolSize())
        .append(this.getAcquireIncrement()).append(getDriverImp()).toHashCode();
  }

  public int getMinimumPoolSize() {
    return minimumPoolSize;
  }

  public void setMinimumPoolSize(int minimumPoolSize) {
    this.minimumPoolSize = minimumPoolSize;
  }

  public int getMaximumPoolSize() {
    return maximumPoolSize;
  }

  public void setMaximumPoolSize(int maximumPoolSize) {
    this.maximumPoolSize = maximumPoolSize;
  }

  public int getAcquireIncrement() {
    return acquireIncrement;
  }

  public void setAcquireIncrement(int acquireIncrement) {
    this.acquireIncrement = acquireIncrement;
  }

  public String getConnectUrl() {
    return connectUrl;
  }

  public void setConnectUrl(String connectUrl) {
    this.connectUrl = connectUrl;
  }

  public TimeInterval getConnectionAcquireWait() {
    return connectionAcquireWait;
  }

  private int connectionAcquireWait() {
    return Long.valueOf(
        getConnectionAcquireWait() != null ? getConnectionAcquireWait().toMilliseconds() : DEFAULT_CONN_ACQUIRE_WAIT
            .toMilliseconds()).intValue();
  }
  public void setConnectionAcquireWait(TimeInterval connectionAcquireWait) {
    this.connectionAcquireWait = connectionAcquireWait;
  }

  public TimeInterval getIdleConnectionTestPeriod() {
    return idleConnectionTestPeriod;
  }

  private int idleConnectionTestPeriod() {
    return Long.valueOf(
        getIdleConnectionTestPeriod() == null
        ? TimeUnit.MILLISECONDS.toSeconds(DEFAULT_IDLE_TEST_PERIOD.toMilliseconds())
            : TimeUnit.MILLISECONDS.toSeconds(getIdleConnectionTestPeriod().toMilliseconds())).intValue();
  }

  public void setIdleConnectionTestPeriod(TimeInterval idleConnectionTestPeriod) {
    this.idleConnectionTestPeriod = idleConnectionTestPeriod;
  }

  public TimeInterval getMaxIdleTime() {
    return maxIdleTime;
  }

  public void setMaxIdleTime(TimeInterval t) {
    this.maxIdleTime = t;
  }

  private int maxIdleTime() {
    return Long.valueOf(
        getMaxIdleTime() != null ? TimeUnit.MILLISECONDS.toSeconds(getMaxIdleTime().toMilliseconds()) : TimeUnit.MILLISECONDS
            .toSeconds(DEFAULT_MAX_IDLE_TIME.toMilliseconds())).intValue();
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
