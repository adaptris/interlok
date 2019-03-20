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
import java.util.Properties;

import javax.sql.DataSource;
import javax.validation.Valid;

import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AllowsRetriesConnection;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;

/**
 * <p>
 * Abstract class containing configuration for JDBC Connection classes.
 * </p>
 * 
 * @author lchan
 */
public abstract class DatabaseConnection extends AllowsRetriesConnection {

  @AutoPopulated
  @NotBlank
  @InputFieldHint(style = "SQL")
  @AdvancedConfig
  private String testStatement;
  @NotBlank
  @AutoPopulated
  private String driverImp;
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean autoCommit;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean debugMode;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean alwaysValidateConnection;
  private String username;
  @InputFieldHint(style = "PASSWORD", external = true)
  private String password;
  @Valid
  @AdvancedConfig
  private KeyValuePairSet connectionProperties;

  private transient DataSource wrapper;

  enum ConnectionState {
    Closed, Initialising, Initialised, Starting, Started, Stopping, Stopped, Closing
  };

  private transient ConnectionState connectionState;

  /**
   * <p>
   * Creates a new instance. Defaults are auto-commit and MySQL. Always validate is false and the test statement set to
   * <code>SELECT DATABASE(), VERSION(), NOW(), USER();</code>.
   * </p>
   */
  public DatabaseConnection() {
    setDriverImp("com.mysql.jdbc.Driver");
    setTestStatement("SELECT DATABASE(), VERSION(), NOW(), USER();");
    wrapper = new DataSourceWrapper(this);
    connectionState = ConnectionState.Closed;
  }

  @Override
  protected void prepareConnection() throws CoreException {
  }

  /**
   * <p>
   * Initialisation ensures that the configured driver implementation is available for use. It does not perform connection
   * verification. This is done by <code>start</code> or upon the first call to <code>prepareStatement()</code>
   * </p>
   * 
   * @throws CoreException if the driver implementation was not available.
   */
  @Override
  protected final void initConnection() throws CoreException {
    connectionState = ConnectionState.Initialising;
    try {
      Class.forName(getDriverImp());
    }
    catch (ClassNotFoundException e) {
      throw new CoreException("No available driver implementation " + getDriverImp(), e);
    }
    initialiseDatabaseConnection();
    connectionState = ConnectionState.Initialised;
  }

  /**
   * <p>
   * Starting this connection means that an initial attempt is made to connect to the database. The connection could previously have
   * been started by a call to <code>getConnection</code>
   * 
   * @see com.adaptris.core.AdaptrisConnectionImp#startConnection()
   * @see #connect()
   * @throws CoreException if the connection could not be started.
   */
  @Override
  protected final void startConnection() throws CoreException {
    connectionState = ConnectionState.Starting;
    startDatabaseConnection();
    connectionState = ConnectionState.Started;
  }

  /**
   * 
   * @see com.adaptris.core.AdaptrisConnectionImp#stopConnection()
   */
  @Override
  protected final void stopConnection() {
    connectionState = ConnectionState.Stopping;
    stopDatabaseConnection();
    connectionState = ConnectionState.Stopped;
  }

  @Override
  protected final void closeConnection() {
    connectionState = ConnectionState.Closing;
    closeDatabaseConnection();
    connectionState = ConnectionState.Closed;
  }

  /**
   * <p>
   * Sets the driver implementation to use.
   * </p>
   * 
   * @param s the driver implementation to use
   */
  public void setDriverImp(String s) {
    driverImp = s;
  }

  /**
   * <p>
   * Returns the driver implementation to use.
   * </p>
   * 
   * @return the driver implementation to use
   */
  public String getDriverImp() {
    return driverImp;
  }

  /**
   * <p>
   * Sets whether to auto-commit.
   * </p>
   * 
   * @param b whether to auto-commit; default is true.
   */
  public void setAutoCommit(Boolean b) {
    autoCommit = b;
  }

  /**
   * <p>
   * Returns whether to auto-commit.
   * </p>
   * 
   * @return whether to auto-commit
   */
  public Boolean getAutoCommit() {
    return autoCommit;
  }

  public boolean autoCommit() {
    return BooleanUtils.toBooleanDefaultIfNull(getAutoCommit(), true);
  }

  /**
   * <p>
   * Set this connection's debug mode.
   * </p>
   * <p>
   * In debug mode there is additional logging for reconnection attempts.
   * </p>
   * 
   * @param dbg this connection's debug mode
   */
  public void setDebugMode(Boolean dbg) {
    debugMode = dbg;
  }

  /**
   * <p>
   * Returns this connection's debug mode.
   * </p>
   * 
   * @return this connection's debug mode
   */
  public Boolean getDebugMode() {
    return debugMode;
  }

  public boolean debugMode() {
    return BooleanUtils.toBooleanDefaultIfNull(getDebugMode(), false);
  }

  /**
   * Set the SQL statement used to test this connection.
   * <p>
   * The default test statement is <code>SELECT DATABASE(), VERSION(), NOW(), USER()</code> which may not be suitable for your
   * database driver. Additionally depending on the JDBC driver implementation certain statements may be 'cached' and might never
   * hit the database, so you need to be aware of that as you will be relying on this test-statement to verify the connection
   * validity.
   * </p>
   * 
   * @see #setAlwaysValidateConnection(Boolean)
   * @param s the SQL statement used to test this connection; the default is SELECT DATABASE(), VERSION(), NOW(), USER()
   */
  public void setTestStatement(String s) {
    testStatement = s;
  }

  /**
   * <p>
   * Returns the SQL statement used to test this connection.
   * </p>
   * 
   * @return the SQL statement used to test this connection
   */
  public String getTestStatement() {
    return testStatement;
  }

  /**
   * Expose this DatabaseConnection as a DataSource
   * 
   * @return a DataSource implementation
   * @see DataSource
   */
  public DataSource asDataSource() throws SQLException {
//    checkInternalState();
    return wrapper;
  }

  /**
   * <p>
   * Returns the underlying <code>SQLConnection</code>.
   * </p>
   * 
   * @return the underlying <code>SQLConnection</code>
   * @throws SQLException if the connection was not valid, and reconnection failed
   */
  public final Connection connect() throws SQLException {
    return attemptConnect();
  }

  /**
   * <p>
   * Make the connection.
   * </p>
   * <p>
   * This abstract method should be implemented by concrete sub-classes to make or check the current connection to the Jdbc source.
   * </p>
   * 
   * @see #connect()
   * @return a java.sql.Connection
   * @throws SQLException if the connection could not be made.
   */
  protected abstract Connection makeConnection() throws SQLException;

  /**
   * <p>
   * Initialise the underlying database connection.
   * </p>
   * <p>
   * This abstract method should be implemented by concrete sub-classes to initialise any components other than the DriverManager.
   * </p>
   * 
   * @throws CoreException wrapping any underlying exception.
   */
  protected abstract void initialiseDatabaseConnection() throws CoreException;

  /**
   * <p>
   * Initialise the underlying database connection.
   * </p>
   * <p>
   * This abstract method should be implemented by concrete sub-classes to initialise any components other than the DriverManager.
   * </p>
   * 
   * @throws CoreException wrapping any underlying exception.
   */
  protected abstract void startDatabaseConnection() throws CoreException;

  /**
   * <p>
   * Initialise the underlying database connection.
   * </p>
   * <p>
   * This abstract method should be implemented by concrete sub-classes to stop any components other than the DriverManager.
   * </p>
   * 
   */
  protected abstract void stopDatabaseConnection();

  /**
   * <p>
   * Initialise the underlying database connection.
   * </p>
   * <p>
   * This abstract method should be implemented by concrete sub-classes to close any components.
   * </p>
   * 
   */
  protected abstract void closeDatabaseConnection();
  
  /**
   * <p>
   * Returns a name for this connection for logging purposes.
   * </p>
   * 
   * @return a name for this connection for logging purposes
   */
  protected abstract String getConnectionName();

  /**
   * <p>
   * Initiate a connection to the database.
   * </p>
   * 
   * @throws SQLException if connection fails after exhausting the specified number of retry attempts
   */
  private Connection attemptConnect() throws SQLException {
    int attemptCount = 0;
    Connection sqlConnection = null;
    while (sqlConnection == null) {
      checkInternalState();
      try {
        attemptCount++;
        sqlConnection = makeConnection();
        if (sqlConnection.getAutoCommit() != autoCommit()) {
          sqlConnection.setAutoCommit(autoCommit());
        }
      }
      catch (SQLException e) {
        if (logWarning(attemptCount)) {
          log.warn("Connection attempt [{}] failed for {}", attemptCount, getConnectionName(), e);
        }
        if (connectionAttempts() != -1 && attemptCount >= connectionAttempts()) {
          log.error("Failed to make any Jdbc Connections");
          throw e;
        }
        else {
          log.trace(createLoggingStatement(attemptCount));
          try {
            Thread.sleep(connectionRetryInterval());
          }
          catch (InterruptedException e2) {
            throw new SQLException(e2);
          }
          continue;
        }
      }
    }
    return sqlConnection;
  }

  protected void checkInternalState() throws SQLException {
    switch (connectionState) {
    case Starting:
    case Initialised:
    case Initialising:
    case Started:
      break;
    default:
      throw new SQLException("Internal Component state out of sync [" + connectionState + "]");
    }
  }

  /**
   * <p>
   * Set whether to always validate the database connection.
   * </p>
   * <p>
   * Validating the connection means that the test-statement is executed every time {@link DatabaseConnection#connect()} is invoked.
   * Depending on the test statement in question this might have an impact upon performance.
   * </p>
   * 
   * @param b whether to always validate the database connection; defaults to false.
   */
  public void setAlwaysValidateConnection(Boolean b) {
    alwaysValidateConnection = b;
  }

  /**
   * <p>
   * Returns whether to always validate the database connection.
   * </p>
   * 
   * @return whether to always validate the database connection
   */
  public Boolean getAlwaysValidateConnection() {
    return alwaysValidateConnection;
  }

  public boolean alwaysValidateConnection() {
    return BooleanUtils.toBooleanDefaultIfNull(getAlwaysValidateConnection(), false);
  }

  /**
   * <p>
   * Force implementations to over-ride equals with a semantic implementation.
   * </p>
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public abstract boolean equals(Object o);

  /**
   * <p>
   * Force implementations to over-ride hashcode.
   * </p>
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public abstract int hashCode();

  public String getUsername() {
    return username;
  }

  /**
   * Set the username used to access the database.
   * 
   * @param s
   */
  public void setUsername(String s) {
    username = s;
  }

  public String getPassword() {
    return password;
  }

  /**
   * Set the password used to access the database.
   * 
   * @param s the password which might be encoded using an available password scheme from
   *          {@link com.adaptris.security.password.Password}
   */
  public void setPassword(String s) {
    password = s;
  }

  /**
   * Get any additional connection properties that have been configured.
   * 
   * @return any additional properties
   */
  public KeyValuePairSet getConnectionProperties() {
    return connectionProperties;
  }

  /**
   * Set any additional connection properties.
   * <p>
   * If additional connection properties are set, then any configured username/password will be applied to the connection properties
   * against the key <code>'user'</code> and <code>'password'</code> respectively. If you have configured thos properties in the
   * connection-properties element, then make sure that you do not configure {@link #setUsername(String)} or
   * {@link #setPassword(String)}.
   * </p>
   * 
   * @param p any additional properties over and above username/password.
   */
  public void setConnectionProperties(KeyValuePairSet p) {
    this.connectionProperties = p;
  }

  protected Properties connectionProperties() throws PasswordException {
    return JdbcUtil.mergeConnectionProperties(
        getConnectionProperties() != null ? KeyValuePairBag.asProperties(getConnectionProperties()) : new Properties(),
        getUsername(), ExternalResolver.resolve(getPassword()));
  }

}
