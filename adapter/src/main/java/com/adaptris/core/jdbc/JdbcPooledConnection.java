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

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.password.Password;
import com.adaptris.util.TimeInterval;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A {@link DatabaseConnection} instance that provides connection pooling via c3p0.
 * 
 * @author amcgrath
 *
 */
@XStreamAlias("jdbc-pooled-connection")
@AdapterComponent
@ComponentProfile(summary = "Connect to a database using a JDBC driver; connection pooling handled via C3P0",
    tag = "connections,jdbc")
@DisplayOrder(order = {"username", "password", "driverImp", "connectUrl", "minimumPoolSize", "maximumPoolSize", "maxIdleTime",
    "acquireIncrement"})
public class JdbcPooledConnection extends JdbcPooledConnectionImpl implements JdbcPoolConfiguration {
  
  public static final int DEFAULT_MINIMUM_POOL_SIZE = 5;
  public static final int DEFAULT_MAXIMUM_POOL_SIZE = 50;
  public static final int DEFAULT_ACQUIRE_INCREMENT = 5;
  
  private static final TimeInterval DEFAULT_CONN_ACQUIRE_WAIT = new TimeInterval(5L, TimeUnit.SECONDS);
  private static final TimeInterval DEFAULT_IDLE_TEST_PERIOD = new TimeInterval(60L, TimeUnit.SECONDS);
  private static final TimeInterval DEFAULT_MAX_IDLE_TIME = new TimeInterval(10L, TimeUnit.MINUTES);
  private static final JdbcPoolFactory DEFAULT_POOL_FACTORY = new DefaultPoolFactory();


  @InputFieldDefault(value = "5")
  private Integer minimumPoolSize;
  @InputFieldDefault(value = "50")
  private Integer maximumPoolSize;
  @AdvancedConfig
  @InputFieldDefault(value = "5")
  private Integer acquireIncrement;
  @Valid
  @AdvancedConfig
  private TimeInterval connectionAcquireWait;
  @Valid
  @AdvancedConfig
  private TimeInterval idleConnectionTestPeriod;
  @Valid
  @AdvancedConfig
  private TimeInterval maxIdleTime;
  @AdvancedConfig
  @Valid
  private JdbcPoolFactory poolFactory;

  
  public JdbcPooledConnection () {
    super();
  }
  

  protected synchronized ComboPooledDataSource createPool() throws CoreException {
    ComboPooledDataSource pool = poolFactory().create(this);
    try {
      pool.setProperties(connectionProperties());
      pool.setDriverClass(this.getDriverImp());
      pool.setJdbcUrl(this.getConnectUrl());
      pool.setUser(this.getUsername());
      pool.setPassword(Password.decode(ExternalResolver.resolve(this.getPassword())));
    }
    catch (Exception ex) {
      throw ExceptionHelper.wrapCoreException(ex);
    }
    return pool;
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
          .append(pooledConnection.getAcquireIncrement(), this.getAcquireIncrement())
          .append(pooledConnection.getPoolFactory(), this.getPoolFactory()).isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(19, 31).append(this.getConnectUrl()).append(this.getMinimumPoolSize()).append(this.getMaximumPoolSize())
        .append(this.getAcquireIncrement()).append(getDriverImp()).append(getPoolFactory()).toHashCode();
  }

  public Integer getMinimumPoolSize() {
    return minimumPoolSize;
  }

  public void setMinimumPoolSize(Integer minimumPoolSize) {
    this.minimumPoolSize = minimumPoolSize;
  }

  public int minPoolSize() {
    return getMinimumPoolSize() != null ? getMinimumPoolSize().intValue() : DEFAULT_MINIMUM_POOL_SIZE;
  }

  public Integer getMaximumPoolSize() {
    return maximumPoolSize;
  }

  public void setMaximumPoolSize(Integer maximumPoolSize) {
    this.maximumPoolSize = maximumPoolSize;
  }

  public int maxPoolSize() {
    return getMaximumPoolSize() != null ? getMaximumPoolSize().intValue() : DEFAULT_MAXIMUM_POOL_SIZE;
  }

  public Integer getAcquireIncrement() {
    return acquireIncrement;
  }

  public void setAcquireIncrement(Integer acquireIncrement) {
    this.acquireIncrement = acquireIncrement;
  }

  public int acquireIncrement() {
    return getAcquireIncrement() != null ? getAcquireIncrement().intValue() : DEFAULT_ACQUIRE_INCREMENT;
  }

  public TimeInterval getConnectionAcquireWait() {
    return connectionAcquireWait;
  }

  public int connectionAcquireWait() {
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

  public int idleConnectionTestPeriod() {
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

  public int maxIdleTime() {
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

  /**
   * @return the poolFactory
   */
  public JdbcPoolFactory getPoolFactory() {
    return poolFactory;
  }

  /**
   * @param f the poolFactory to set
   */
  public void setPoolFactory(JdbcPoolFactory f) {
    this.poolFactory = f;
  }

  JdbcPoolFactory poolFactory() {
    return getPoolFactory() != null ? getPoolFactory() : DEFAULT_POOL_FACTORY;
  }
}
