package com.adaptris.core.jdbc;

import static com.adaptris.core.jdbc.PooledConnectionProperties.searchEnumIgnoreCase;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.security.password.Password;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("advanced-jdbc-pooled-connection")
@AdapterComponent
@ComponentProfile(summary = "Connect to a database using a JDBC driver; connection pooling handled via C3P0",
    tag = "connections,jdbc")
@DisplayOrder(order = {"username", "password", "driverImp", "connectUrl", "connectionProperties"})
public class AdvancedJdbcPooledConnection extends DatabaseConnection {
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @NotBlank
  private String connectUrl;
  
  @NotNull
  @AutoPopulated
  @Valid
  private KeyValuePairSet connectionProperties;
  
  private transient ComboPooledDataSource connectionPool;
  
  public AdvancedJdbcPooledConnection() {
    super();
    this.setConnectionPoolProperties(new KeyValuePairSet());
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
      
      for (KeyValuePair kvp : getConnectionPoolProperties().getKeyValuePairs()) {
        PooledConnectionProperties connectionProperty = searchEnumIgnoreCase(kvp.getKey());
        if(connectionProperty != null) {
          connectionProperty.applyProperty(connectionPool, kvp.getValue());
        } else
          log.warn("Property {} not found, ignored.", kvp.getKey());
      }
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
      AdvancedJdbcPooledConnection pooledConnection = (AdvancedJdbcPooledConnection) ajpc;
      
      return new EqualsBuilder()
        .append(pooledConnection.getConnectUrl(), this.getConnectUrl())
        .append(pooledConnection.getDriverImp(), this.getDriverImp())
        .append(pooledConnection.getConnectionPoolProperties(), this.getConnectionPoolProperties())
        .isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 31)
        .append(this.getConnectUrl())
        .append(this.getConnectionPoolProperties())
        .append(getDriverImp())
        .toHashCode();
  }

  public String getConnectUrl() {
    return connectUrl;
  }

  public void setConnectUrl(String connectUrl) {
    this.connectUrl = connectUrl;
  }

  public KeyValuePairSet getConnectionPoolProperties() {
    return connectionProperties;
  }

  public void setConnectionPoolProperties(KeyValuePairSet connectionProperties) {
    this.connectionProperties = connectionProperties;
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
