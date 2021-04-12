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

import java.sql.SQLException;
import javax.validation.Valid;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.interlok.resolver.ExternalResolver;
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
public class AdvancedJdbcPooledConnection extends JdbcPooledConnectionImpl {
  
  @Valid
  private KeyValuePairSet connectionPoolProperties;

  public AdvancedJdbcPooledConnection() {
    super();
  }
  
  
  @Override
  protected C3P0PooledDataSource createPool() throws Exception {
    ComboPooledDataSource pool = new ComboPooledDataSource();
    pool = new ComboPooledDataSource();
    pool.setProperties(connectionProperties());
    pool.setDriverClass(getDriverImp());
    pool.setJdbcUrl(getConnectUrl());
    pool.setUser(getUsername());
    pool.setPassword(Password.decode(ExternalResolver.resolve(getPassword())));

    pool.setAcquireRetryDelay(Long.valueOf(connectionRetryInterval()).intValue());
    pool.setAcquireRetryAttempts(connectionAttempts());
    pool.setTestConnectionOnCheckin(alwaysValidateConnection());
    pool.setTestConnectionOnCheckout(alwaysValidateConnection());
    PooledConnectionProperties.apply(getConnectionPoolProperties(), pool);
    return new C3P0PooledDataSource(pool);
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
          .append(conn.getConnectUrl(), getConnectUrl())
          .append(conn.getDriverImp(), getDriverImp())
          .append(conn.getAlwaysValidateConnection(), getAlwaysValidateConnection())
          .append(conn.getDebugMode(), getDebugMode())
          .append(conn.getAutoCommit(), getAutoCommit())
          .append(conn.getConnectionProperties(), getConnectionProperties())
          .append(conn.getConnectionPoolProperties(), getConnectionPoolProperties())
          .isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 31)
        .append(getConnectUrl())
        .append(getDriverImp())
        .append(getAlwaysValidateConnection())
        .append(getDebugMode())
        .append(getAutoCommit())
        .append(getConnectionProperties())
        .append(getConnectionPoolProperties())
        .toHashCode();
  }

  public KeyValuePairSet getConnectionPoolProperties() {
    return connectionPoolProperties;
  }

  public void setConnectionPoolProperties(KeyValuePairSet kvps) {
    connectionPoolProperties = kvps;
  }

  public int currentBusyConnectionCount() throws SQLException {
    return ((C3P0PooledDataSource) connectionPool).wrapped().getNumBusyConnections();
  }

  public int currentConnectionCount() throws SQLException {
    return ((C3P0PooledDataSource) connectionPool).wrapped().getNumConnections();
  }

  public int currentIdleConnectionCount() throws SQLException {
    return ((C3P0PooledDataSource) connectionPool).wrapped().getNumIdleConnections();
  }


}
